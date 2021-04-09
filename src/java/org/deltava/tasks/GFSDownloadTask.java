// Copyright 2013, 2014, 2015, 2016, 2017, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.io.*;
import java.util.*;
import java.time.Instant;
import java.sql.Connection;
import java.util.concurrent.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;
import org.deltava.beans.wx.*;

import org.deltava.dao.*;
import org.deltava.dao.file.*;
import org.deltava.taskman.*;

import org.deltava.util.*;
import org.deltava.util.ftp.*;
import org.deltava.util.tile.*;
import org.deltava.util.system.SystemData;

import org.gvagroup.tile.*;

/**
 * A scheduled task to download GFS global forecast data.
 * @author Luke
 * @version 10.0
 * @since 5.2
 */

public class GFSDownloadTask extends Task {
	
	private static final List<PressureLevel> LEVELS = List.of(PressureLevel.JET);

	/**
	 * Initializes the Task.
	 */
	public GFSDownloadTask() {
		super("GFS Download", GFSDownloadTask.class);
	}
	
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
				String gribPath = basePath + "/" + dir + "/" + hDir + "/atmos";
				String fName = con.getNewest(gribPath, FileUtils.fileFilter("gfs.", ".pgrb2.0p25.f000"));
				if (!StringUtils.isEmpty(fName)) {
					Instant lm = con.getTimestamp(gribPath, fName);
					log.info(fName + " timestamp = " + StringUtils.format(lm, "MM/dd HH:mm"));
					log.info("Local timestamp = " + StringUtils.format(Instant.ofEpochMilli(outF.lastModified()), "MM/dd HH:mm"));
				
					// Calculate the effective date and download
					dt = StringUtils.parseInstant(dir.substring(dir.lastIndexOf('.') + 1) + hDir, "yyyyMMddHH");
					if (!outF.exists() || (lm.toEpochMilli() > outF.lastModified())) {
						log.info("Downloading updated GFS data");
						TaskTimer tt = new TaskTimer(); 
						try (InputStream in = con.get(gribPath + "/" + fName, outF)) {
							log.info("Downloaded GFS data - " + outF.length());
							outF.setLastModified(lm.toEpochMilli());
							log.info("Download completed in " + tt.stop() + "ms");
						}
					}
				} else
					log.warn("GRIB not ready yet");
			}
		} catch (FTPClientException | IOException e) {
			log.error("Error processing GFS data - " + e.getMessage(), e);
			return;
		}
		
		// Get/set the cycle
		try {
			Connection con = ctx.getConnection();
			if (dt == null) {
				GetMetadata mddao = new GetMetadata(con);
				dt = Instant.ofEpochSecond(Long.parseLong(mddao.get("gfs.cycle")));
				log.info("Reusing " + dt + " GFS data");
			} else {
				SetMetadata mdwdao = new SetMetadata(con);
				mdwdao.write("gfs.cycle", dt);
			}
		} catch(DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}
			
		// Plot the tiles
		int threads = Math.max(3, Runtime.getRuntime().availableProcessors() + 1);
		log.info("Running " + threads + " Tile workers");
		try (GetWAFSData dao = new GetWAFSData(outF.getAbsolutePath())) {
			BlockingQueue<TileAddress> work = new LinkedBlockingQueue<TileAddress>();
			for (PressureLevel lvl : LEVELS) {
				TaskTimer tt = new TaskTimer();
				GRIBResult<WindData> data = dao.load(lvl);
				log.info(lvl.getPressure() + "mb data loaded in " + tt.stop() + "ms");
				
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
				tt.start(); ImageSeries is = new ImageSeries("wind-" + lvl.name().toLowerCase(), dt);
				Collection<GFSTileWorker> workers = new ArrayList<GFSTileWorker>();
				for (int x = 0; x <= threads; x++) {
					GFSTileWorker tw = new GFSTileWorker(x+1, work, data, is);
					workers.add(tw);
					tw.start();
				}

				ThreadUtils.waitOnPool(workers);
				log.info(lvl.getPressure() + "mb Tiles plotted in " + tt.stop() + "ms");
				
				// Get existing tile layers
				GetTiles trdao = new GetTiles();
				Collection<Instant> seriesDates = trdao.getDates(is.getType());
				seriesDates.remove(is.getDate());

				// Save the tiles and purge older dates
				SetTiles twdao = new SetTiles();
				twdao.write(is);
				for (Instant sd : seriesDates) {
					log.info("Purging " + is.getType() + " / " + StringUtils.format(sd, "MM/dd HH:mm"));
					twdao.purge(new ImageSeries(is.getType(), sd));
				}
			}
		} catch (Exception e) {
			log.error("Error processing GFS data - " + e.getMessage(), e);
		}

		log.info("Processing Complete");
	}
}