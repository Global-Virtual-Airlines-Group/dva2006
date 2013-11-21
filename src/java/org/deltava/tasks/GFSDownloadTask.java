// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.awt.Color;
import java.awt.image.BufferedImage;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.wx.*;

import org.deltava.dao.*;
import org.deltava.dao.file.GetWAFSData;
import org.deltava.dao.mc.SetTiles;

import org.deltava.taskman.*;

import org.deltava.util.*;
import org.deltava.util.tile.*;
import org.deltava.util.ftp.FTPConnection;
import org.deltava.util.system.SystemData;

/**
 * A scheduled task to download GFS global forecast data.
 * @author Luke
 * @version 5.2
 * @since 5.2
 */

public class GFSDownloadTask extends Task {

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
				SingleTile st = new SingleTile(addr);
				st.setImage(img); boolean hasData = false;
				for (int x = 0; x < Tile.WIDTH; x++) {
					for (int y = 0; y < Tile.HEIGHT; y++) {
						GeoLocation loc = p.getGeoPosition(pX + x, pY + y);
						if (Math.abs(loc.getLatitude()) > 81)
							continue;

						WindData wd = _data.getResult(loc);
						if (wd.getJetStreamSpeed() < 40)
							continue;
						
						int c = Math.min(255, wd.getJetStreamSpeed() + 40);
						if (wd.getJetStreamSpeed() > 99) {
							int r = Math.min(c+32, 255);
							int g = (wd.getJetStreamSpeed() > 150) ? Math.min(255, c+36): c;
							Color rgb = new Color(r,g,c);
							img.setRGB(x, y, rgb.getRGB());
						} else
							img.setRGB(x, y, new Color(c, c, c).getRGB());
						
						hasData = true;
					}
				}

				// Convert to PNG
				if (hasData) {
					try (ByteArrayOutputStream fo = new ByteArrayOutputStream(512)) {
						PNGTile png = new PNGTile(st);
						fo.write(png.getData());
						synchronized (_out) {
							_out.add(png);
						}
					} catch (IOException ie) {
						// empty
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
		try {
			File outF = new File(SystemData.get("weather.cache"), "gfs.grib");
			String host = SystemData.get("weather.gfs.host"); ImageSeries is = null;
			try (FTPConnection con = new FTPConnection(host)) {
				con.connect("anonymous", "webmaster@deltava.org");
				log.info("Connected to " + host);
			
				// Find the latest GFS run and get the latest GFS file
				String dir = con.getNewestDirectory(SystemData.get("weather.gfs.path"), FileUtils.fileFilter("gfs.", null));
				String fName = con.getNewest(dir, FileUtils.fileFilter("gfs.", ".pgrb2f00"));
				
				// Calculate the effective date
				is = new ImageSeries("jetstream", StringUtils.parseDate(dir, "yyyyMMddHH"));
			
				// Download
				try (InputStream in = con.get(fName, outF)) {
					log.info("Downloaded GFS data - " + outF.length());
				}
			}
			
			// Load the GFS data
			GetWAFSData dao = new GetWAFSData(outF.getAbsolutePath());
			GRIBResult<WindData> data = dao.load();
			
			// Write the data to the database
			try {
				ctx.startTX();
				SetWeather wwdao = new SetWeather(ctx.getConnection());
				wwdao.purgeWinds(5);
				
				for (WindData wd : data)
					wwdao.write(is.getDate(), wd);
				
				ctx.commitTX();
			} catch(DAOException de) {
				ctx.rollbackTX();
			} finally {
				ctx.release();
			}
			
			// Figure out the tiles to plot
			BlockingQueue<TileAddress> work = new LinkedBlockingQueue<TileAddress>();
			for (int zoom = 5; zoom > 1; zoom--) {
				Projection p = new MercatorProjection(zoom);
				TileAddress nw = p.getAddress(data.getNW()); TileAddress se = p.getAddress(data.getSE());
				for (int tx = nw.getX(); tx <= se.getX(); tx++) {
					for (int ty = nw.getY(); ty <= se.getY(); ty++)
						work.add(new TileAddress(tx, ty, zoom));
				}
			}
			
			// Plot the tiles
			long startTime = System.currentTimeMillis();
			Collection<TileWorker> workers = new ArrayList<TileWorker>();
			int threads = Math.max(2, Runtime.getRuntime().availableProcessors() - 1);
			for (int x = 0; x <= threads; x++) {
				TileWorker tw = new TileWorker(x+1, work, data, is);
				tw.setPriority(Thread.MIN_PRIORITY);
				workers.add(tw);
				tw.start();
			}
			
			ThreadUtils.waitOnPool(workers);
			log.info("Workers completed in " + (System.currentTimeMillis() - startTime) + "ms");
			
			// Save in memcached
			SetTiles twdao = new SetTiles();
			twdao.write(is);
		} catch (Exception e) {
			log.error("Error processing GFS data - " + e.getMessage(), e);
		}

		log.info("Processing Complete");
	}
}