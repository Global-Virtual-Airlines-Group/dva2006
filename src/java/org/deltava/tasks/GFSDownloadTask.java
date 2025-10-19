// Copyright 2013, 2014, 2015, 2016, 2017, 2021, 2022, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.temporal.ChronoField;
import java.sql.Connection;
import java.util.concurrent.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;
import org.deltava.beans.wx.*;

import org.deltava.dao.*;
import org.deltava.dao.file.*;
import org.deltava.dao.http.*;
import org.deltava.taskman.*;

import org.deltava.util.*;
import org.deltava.util.tile.*;
import org.deltava.util.system.SystemData;

/**
 * A scheduled task to download GFS global forecast data.
 * @author Luke
 * @version 12.3
 * @since 5.2
 */

public class GFSDownloadTask extends Task {
	
	private static final List<PressureLevel> LEVELS = List.of(PressureLevel.LOJET, PressureLevel.JET);

	/**
	 * Initializes the Task.
	 */
	public GFSDownloadTask() {
		super("GFS Download", GFSDownloadTask.class);
	}
	
	@Override
	protected void execute(TaskContext ctx) {
		
		// Determine hour to download
		ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
		int hour = (now.get(ChronoField.HOUR_OF_DAY) - 3) / 6; String hr = StringUtils.format(hour * 6, "00");
		
		// Build URL components
		String urlPath = String.format("%s/gfs.%s/%s/atmos", SystemData.get("weather.gfs.path"), StringUtils.format(now, "YYYYMMdd"), hr);
		String urlFile = String.format("gfs.t%sz.pgrb2.0p25.f000", hr);
		String url = String.format("https://%s%s/%s", SystemData.get("weather.gfs.host"), urlPath, urlFile);
		
		File outF = new File(SystemData.get("weather.cache"), "gfs.grib"); Instant dt = null;
		try {
			log.info("Fetching GFS data from {}", url);
			TaskTimer tt = new TaskTimer();
			GetURL urldao = new GetURL(url, outF.getAbsolutePath());
			urldao.setCompression(Compression.GZIP, Compression.BROTLI);
			File f = urldao.download();
			log.info("Downloaded GFS data ({} bytes) in {}ms", Long.valueOf(outF.length()), Long.valueOf(tt.stop()));
			
			// Update last mofiedied date to be effective date
			LocalDateTime ld = LocalDateTime.of(LocalDate.now(), LocalTime.of(hour, 0));
			dt = ld.toInstant(ZoneOffset.UTC);
			f.setLastModified(dt.toEpochMilli());
			
			// Get/set the cycle
			Connection con = ctx.getConnection();
			SetMetadata mdwdao = new SetMetadata(con);
			mdwdao.write("gfs.cycle", dt);
		} catch (DAOException de) {
			log.atError().withThrowable(de).log("Error processing GFS data - {}", de.getMessage());
			return;
		} finally {
			ctx.release();
		}
			
		// Plot the tiles
		int threads = Math.max(3, Runtime.getRuntime().availableProcessors() + 1);
		log.info("Running {} Tile workers", Integer.valueOf(threads));
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
				log.info("{}mb Tiles plotted in {}ms", Integer.valueOf(lvl.getPressure()), Long.valueOf(tt.stop()));
				
				// Get existing tile layers
				GetTiles trdao = new GetTiles();
				Collection<Instant> seriesDates = trdao.getDates(is.getType());
				seriesDates.remove(is.getDate());

				// Save the tiles and purge older dates
				SetTiles twdao = new SetTiles();
				twdao.write(is);
				for (Instant sd : seriesDates) {
					log.info("Purging {} / {}", is.getType(), StringUtils.format(sd, "MM/dd HH:mm"));
					twdao.purge(new ImageSeries(is.getType(), sd));
				}
			}
		} catch (Exception e) {
			log.atError().withThrowable(e).log("Error processing GFS data - {}", e.getMessage());
		}

		log.info("Processing Complete");
	}
}