// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.util.concurrent.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.dao.DAOException;
import org.deltava.dao.http.GetWUImagery;
import org.deltava.dao.mc.SetTiles;

import org.deltava.taskman.*;
import org.deltava.util.tile.*;
import org.deltava.util.system.SystemData;

/**
 * A scheduled task to load Weather Underground radar images. 
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class RadarLoadTask extends Task {
	
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
			
			GetWUImagery dao = new GetWUImagery();
			dao.setConnectTimeout(2000);
			dao.setReadTimeout(5000);

			GeoLocation loc = _p.getGeoPosition(_addr.getPixelX(), _addr.getPixelY());
			try {
				SuperTile st = dao.getRadar(loc, 1280, 1024, _p.getZoomLevel());
				_gt.add(st);
				log.info("Loaded " + _addr);
			} catch (DAOException de) {
				log.error("Cannot load SuperTile at " + _addr, de);
			}
		}
	}

	/**
	 * Initializes the Task.
	 */
	public RadarLoadTask() {
		super("Radar SuperTile loader", RadarLoadTask.class);
	}

	/**
	 * Executes the task.
	 */
	@Override
	protected void execute(TaskContext ctx) {
		
		// Get bounds
		GeoLocation nw = new GeoPosition(SystemData.getDouble("weather.radar.nw.lat", 48), SystemData.getDouble("weather.radar.nw.lng", -127));
		GeoLocation se = new GeoPosition(SystemData.getDouble("weather.radar.se.lat", 21), SystemData.getDouble("weather.radar.se.lng", -62));
		
		// Get tile address bounds
		TileAddress nwAddr = _p.getAddress(nw);
		TileAddress seAddr = _p.getAddress(se);

		// Get the tile addresses
		Collection<TileAddress> work = new ArrayList<TileAddress>();
		for (int x = nwAddr.getX(); x <= seAddr.getX(); x += 5) {
			for (int y = nwAddr.getY(); y <= seAddr.getY(); y += 4) {
				TileAddress addr = new TileAddress(x, y, _p.getZoomLevel());
				work.add(addr);
			}
		}
		
		// Create the executor
		int maxThreads = SystemData.getInt("weather.radar.threads", 12);
		ThreadPoolExecutor exec = new ThreadPoolExecutor(1, maxThreads, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		SparseGlobalTile gt = new SparseGlobalTile(_p.getZoomLevel());
		for (TileAddress addr : work)
			exec.execute(new SuperTileWorker(gt, addr));
		
		// Wait on workers
		try {
			exec.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException ie) {
			log.warn("Timed out loading radar!");
		}
		
		// Do the scale
		SparseGlobalTile cgt = gt;
		List<SparseGlobalTile> levels = new ArrayList<SparseGlobalTile>();
		levels.add(cgt);
		while (cgt.getLevel() > 1) {
			TileScaler ts = new TileScaler(4, cgt);
			ts.run();
			cgt = ts.getResults();
			levels.add(cgt);
		}
		
		// Do the conversion to PNG
		TileCompressor tc = new TileCompressor(4, levels);
		tc.run();
		
		// Save the tiles somewhere
		ImageSeries is = new ImageSeries("radar", new Date());
		is.addAll(tc.getResults());
		try {
			SetTiles stwdao = new SetTiles();
			stwdao.write(is);
		} catch (DAOException de) {
			log.error("Error writing image series", de);
		}
	}
}