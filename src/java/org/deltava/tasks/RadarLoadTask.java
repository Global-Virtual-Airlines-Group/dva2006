// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.util.concurrent.*;

import org.apache.log4j.Logger;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.dao.DAOException;
import org.deltava.dao.http.GetWUImagery;
import org.deltava.dao.mc.SetTiles;

import org.deltava.taskman.*;
import org.deltava.util.tile.*;

import org.deltava.util.CalendarUtils;
import org.deltava.util.TaskTimer;
import org.deltava.util.system.SystemData;

/**
 * A scheduled task to load Weather Underground radar images. 
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class RadarLoadTask extends Task {
	
	private final SeriesWriter _sw;
	protected final Projection _p = new MercatorProjection(SystemData.getInt("weather.radar.zoom", 8));
	
	private final class SuperTileWorker implements Runnable {
		private final SparseGlobalTile _gt;
		private final TileAddress _addr;
		
		SuperTileWorker(SparseGlobalTile gt, TileAddress addr) {
			super();
			_gt = gt;
			_addr = addr;
		}
		
		@Override
		public void run() {
			Logger tLog = Logger.getLogger(RadarLoadTask.class.getPackage().getName() + "-" + Thread.currentThread().getName());
			
			GetWUImagery dao = new GetWUImagery();
			dao.setConnectTimeout(2500);
			dao.setReadTimeout(7500);

			GeoLocation loc = _p.getGeoPosition(_addr.getPixelX(), _addr.getPixelY());
			try {
				SuperTile st = null;
				try {
					st = dao.getRadar(loc, 1024, 1024, _p.getZoomLevel());
				} catch (Exception e) {
					tLog.warn("Error loading " + _addr + " - " + e.getMessage());
					st = dao.getRadar(loc, 1024, 1024, _p.getZoomLevel());
				}
					
				_gt.add(st);
				tLog.info("Loaded " + _addr);
			} catch (DAOException de) {
				tLog.error("Cannot load SuperTile at " + _addr, de);
			}
		}
	}

	/**
	 * Initializes the Task.
	 */
	public RadarLoadTask() {
		this(new SetTiles() {{ setExpiry(3600); }});
	}
	
	/**
	 * Initializes the task with a specific SeriesWriter. Typically used for testing.
	 * @param sw the SeriesWriter
	 */
	public RadarLoadTask(SeriesWriter sw) {
		super("Radar SuperTile loader", RadarLoadTask.class);
		_sw = sw;
	}

	/**
	 * Executes the task.
	 */
	@Override
	protected void execute(TaskContext ctx) {
		
		// Round date downwards to nearest minute
		Calendar cld = CalendarUtils.getInstance(new Date(), false);
		cld.set(Calendar.SECOND, 0);
		cld.set(Calendar.MILLISECOND, 0);
		
		// Get bounds
		GeoLocation nw = new GeoPosition(SystemData.getDouble("weather.radar.nw.lat", 48), SystemData.getDouble("weather.radar.nw.lng", -127));
		GeoLocation se = new GeoPosition(SystemData.getDouble("weather.radar.se.lat", 21), SystemData.getDouble("weather.radar.se.lng", -62));
		
		// Get tile address bounds
		TileAddress nwAddr = _p.getAddress(nw);
		TileAddress seAddr = _p.getAddress(se);

		// Create the executor and get the tile addresses
		int maxThreads = SystemData.getInt("weather.radar.threads", 12);
		ThreadPoolExecutor exec = new ThreadPoolExecutor(maxThreads, maxThreads, 200, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		exec.allowCoreThreadTimeOut(true);
		SparseGlobalTile gt = new SparseGlobalTile(_p.getZoomLevel());
		TaskTimer tt = new TaskTimer();
		for (int x = nwAddr.getX(); x <= seAddr.getX(); x += 4) {
			for (int y = nwAddr.getY(); y <= seAddr.getY(); y += 4) {
				TileAddress addr = new TileAddress(x, y, _p.getZoomLevel());
				exec.execute(new SuperTileWorker(gt, addr));
			}
		}
		
		// Wait on workers
		exec.shutdown();
		try {
			exec.awaitTermination(60, TimeUnit.SECONDS);
			log.info("Loading completed in " + tt.stop() + "ms");
		} catch (InterruptedException ie) {
			log.warn("Timed out loading radar!");
		}
		
		// Do the scale
		tt.start();
		SparseGlobalTile cgt = gt;
		List<SparseGlobalTile> levels = new ArrayList<SparseGlobalTile>();
		levels.add(cgt);
		while (cgt.getLevel() > 1) {
			TileScaler ts = new TileScaler(4, cgt);
			ts.run();
			cgt = ts.getResults();
			levels.add(cgt);
		}
		
		log.info("Scaling completed in " + tt.stop() + "ms");
		
		// Do the conversion to PNG
		tt.start();
		TileCompressor tc = new TileCompressor(4, levels);
		tc.run();
		log.info("Compression completed in " + tt.stop() + "ms");
		
		// Save the tiles somewhere
		tt.start();
		ImageSeries is = new ImageSeries("radar", cld.getTime());
		is.addAll(tc.getResults());
		try {
			_sw.write(is);
			log.info("Serialization completed in " + tt.stop() + "ms");
		} catch (DAOException de) {
			log.error("Error writing image series", de);
		}
	}
}