// Copyright 2013, 2014, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.time.Instant;
import java.awt.Color;
import java.awt.image.BufferedImage;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;
import org.deltava.beans.wx.*;

import org.deltava.dao.*;
import org.deltava.dao.file.GetWAFSData;
import org.deltava.dao.redis.*;
import org.deltava.taskman.*;

import org.deltava.util.*;
import org.deltava.util.ftp.*;
import org.deltava.util.tile.*;
import org.deltava.util.system.SystemData;

import org.gvagroup.tile.*;

/**
 * A scheduled task to download GFS global forecast data.
 * @author Luke
 * @version 8.1
 * @since 5.2
 */

public class GFSDownloadTask extends Task {
	
	private static final List<PressureLevel> LEVELS = Arrays.asList(PressureLevel.JET, PressureLevel.LOJET, PressureLevel.HIGH);

	/**
	 * Initializes the Task.
	 */
	public GFSDownloadTask() {
		super("GFS Download", GFSDownloadTask.class);
	}
	
	private class TileWorker extends Thread {
		private final BlockingQueue<TileAddress> _work;
		private final GRIBResult<WindData> _data;
		private final ImageSeries _out;

		TileWorker(int id, BlockingQueue<TileAddress> work, GRIBResult<WindData> data, ImageSeries out) {
			super("TileWorker-"+ id);
			setDaemon(true);
			_work = work;
			_data = data;
			_out = out;
		}

		@Override
		public void run() {
			TileAddress addr = _work.poll();
			while (addr != null) {
				Projection p = new MercatorProjection(addr.getLevel());

				// Plot the pixels
				int pX = addr.getPixelX(); int pY = addr.getPixelY();
				BufferedImage img = new BufferedImage(Tile.WIDTH, Tile.HEIGHT, BufferedImage.TYPE_INT_ARGB);
				SingleTile st = new SingleTile(addr, img);
				boolean hasData = false;
				for (int x = 0; x < Tile.WIDTH; x++) {
					for (int y = 0; y < Tile.HEIGHT; y++) {
						GeoLocation loc = p.getGeoPosition(pX + x, pY + y);
						if (Math.abs(loc.getLatitude()) > 80.5)
							continue;

						WindData wd = _data.getResult(loc);
						if (wd.getJetStreamSpeed() < 25)
							continue;
						
						int c = Math.min(255, wd.getJetStreamSpeed() + 40);
						if (wd.getJetStreamSpeed() > 60) {
							int r = Math.min(255, c+24);
							int g = (wd.getJetStreamSpeed() > 120) ? Math.min(255, c+32): c;
							Color rgb = new Color(r,g,c);
							img.setRGB(x, y, rgb.getRGB());
						} else {
							int rgb = (c << 16) + (c << 8) + c;
							img.setRGB(x, y, rgb);
						}
						
						hasData = true;
					}
				}

				// Convert to PNG
				if (hasData) {
					PNGTile png = new PNGTile(st.getAddress(), st.getImage());
					synchronized (_out) {
						_out.put(png.getAddress(), png);
					}
				}

				addr = _work.poll();
			}
		}
	}

	/**
	 * Executes the task.
	 * @param ctx the task context
	 */
	@Override
	protected void execute(TaskContext ctx) {
		
		File outF = new File(SystemData.get("weather.cache"), "gfs.grib"); Instant dt = null;
		try {
			String host = SystemData.get("weather.gfs.host");
			try (FTPConnection con = new FTPConnection(host)) {
				con.connect("anonymous", SystemData.get("airline.mail.webmaster"));
				log.info("Connected to " + host);
			
				// Find the latest GFS run and get the latest GFS file
				String basePath = SystemData.get("weather.gfs.path");
				String dir = con.getNewestDirectory(basePath, FileUtils.fileFilter("gfs.", null));
				String hDir = con.getNewestDirectory(basePath + "/" + dir, FileUtils.ACCEPT_ALL);
				String gribPath = basePath + "/" + dir + "/" + hDir;
				String fName = con.getNewest(gribPath, FileUtils.fileFilter("gfs.", ".pgrb2b.0p25.f000"));
				Instant lm = con.getTimestamp(gribPath, fName);
				log.info(fName + " timestamp = " + StringUtils.format(lm, "MM/dd HH:mm"));
				log.info("Local timestamp = " + StringUtils.format(Instant.ofEpochMilli(outF.lastModified()), "MM/dd HH:mm"));
				
				// Calculate the effective date and download
				dt = StringUtils.parseInstant(dir.substring(dir.lastIndexOf('.') + 1) + hDir, "yyyyMMddHH");
				if (!outF.exists() || (lm.toEpochMilli() > outF.lastModified())) {
					log.info("Downloading updated GFS data");
					long startTime = System.currentTimeMillis(); 
					try (InputStream in = con.get(gribPath + "/" + fName, outF)) {
						log.info("Downloaded GFS data - " + outF.length());
						outF.setLastModified(lm.toEpochMilli());
						log.info("Download completed in " + (System.currentTimeMillis() - startTime) + "ms");
					}
				}
			}
		} catch (FTPClientException | IOException e) {
			log.error("Error processing GFS data - " + e.getMessage(), e);
			return;
		}
			
		// Save the winds
		try (GetWAFSData dao = new GetWAFSData(outF.getAbsolutePath())) {
			try {
				long startTime = System.currentTimeMillis();
				SetWinds wwdao = new SetWinds();
				wwdao.setExpiry(18 * 3600);
				for (PressureLevel lvl : PressureLevel.values()) {
					log.info("Loading " + lvl.getPressure() + "mb wind data");
					//GRIBResult<WindData> data = dao.load(lvl);		
					//wwdao.write(data);
				}
				
				// Write GFS cycle data
				SetMetadata mwdao = new SetMetadata(ctx.getConnection());
				mwdao.write("gfs.cycle", dt);
				log.info("Winds written in " + (System.currentTimeMillis() - startTime) + "ms");
			} catch(DAOException de) {
				log.error(de.getMessage(), de);
			} finally {
				ctx.release();
			}
			
			// Plot the tiles
			BlockingQueue<TileAddress> work = new LinkedBlockingQueue<TileAddress>();
			for (PressureLevel lvl : LEVELS) {
				GRIBResult<WindData> data = dao.load(lvl);
				GeoLocation rawNW = data.getNW(); GeoLocation rawSE = data.getSE();
				GeoLocation nwLL = new GeoPosition(Math.min(MercatorProjection.MAX_LATITUDE - 0.2, rawNW.getLatitude()), rawNW.getLongitude() + 0.01);
				GeoLocation seLL = new GeoPosition(Math.max(MercatorProjection.MIN_LATITUDE + 0.2, rawSE.getLatitude()), rawSE.getLongitude() - 0.01);
				for (int zoom = 6; zoom > 1; zoom--) {
					Projection p = new MercatorProjection(zoom);
					TileAddress nw = p.getAddress(nwLL); TileAddress se = p.getAddress(seLL);
					for (int tx = nw.getX(); tx <= se.getX(); tx++) {
						for (int ty = nw.getY(); ty <= se.getY(); ty++)
							work.add(new TileAddress(tx, ty, zoom));
					}
				}
				
				// Plot the tiles
				long startTime = System.currentTimeMillis(); ImageSeries is = new ImageSeries("wind-" + lvl.name().toLowerCase(), null);
				Collection<TileWorker> workers = new ArrayList<TileWorker>();
				int threads = Math.max(4, Runtime.getRuntime().availableProcessors());
				for (int x = 0; x <= threads; x++) {
					TileWorker tw = new TileWorker(x+1, work, data, is);
					tw.setPriority(Thread.MIN_PRIORITY);
					workers.add(tw);
					tw.start();
				}

				ThreadUtils.waitOnPool(workers);
				log.info(lvl.getPressure() + "mb Tiles plotted in " + (System.currentTimeMillis() - startTime) + "ms");

				// Save in memcached
				SetTiles twdao = new SetTiles();
				twdao.purge(is);
				twdao.setExpiry(3600 * 18);
				twdao.write(is);
			}
		} catch (Exception e) {
			log.error("Error processing GFS data - " + e.getMessage(), e);
		}

		log.info("Processing Complete");
	}
}