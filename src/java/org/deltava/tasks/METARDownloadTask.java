// Copyright 2009, 2011, 2015, 2016, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;
import java.time.ZonedDateTime;

import org.deltava.beans.navdata.AirportLocation;
import org.deltava.beans.wx.*;

import org.deltava.dao.*;
import org.deltava.dao.http.*;

import org.deltava.taskman.*;
import org.deltava.util.StringUtils;

/**
 * A Scheduled Task to download METAR data.
 * @author Luke
 * @version 11.4
 * @since 2.7
 */

public class METARDownloadTask extends Task {
	
	private static int PURGE_AGE = 270;

	/**
	 * Initializes the Task.
	 */
	public METARDownloadTask() {
		super("METAR Download", METARDownloadTask.class);
	}

	@Override
	protected void execute(TaskContext ctx) {
		
		int hour = ZonedDateTime.now().getHour();
		try {
			GetNOAAWeather wxdao = new GetNOAAWeather();
			wxdao.setCompression(Compression.GZIP, Compression.BROTLI);
			log.info("Loading METAR cycle for {}00Z", StringUtils.format(hour, "00"));
			Map<String, METAR> data = wxdao.getMETARCycle(hour);
			for (METAR m : data.values())
				m.setILS(WeatherUtils.getILS(m));
			
			// Get the DAOs
			Connection con = ctx.getConnection();
			GetNavData nddao = new GetNavData(con);
			SetWeather wxwdao = new SetWeather(con);
			ctx.startTX();
			
			// Get airport location
			for (METAR m : data.values()) {
				AirportLocation al = nddao.getAirport(m.getCode());
				if (al != null)
					m.setAirport(al);
			}
			
			// Purge the data
			int purgeCount = wxwdao.purgeMETAR(270);
			log.info("Purged {} METAR entries older than {} minutes", Integer.valueOf(purgeCount), Integer.valueOf(PURGE_AGE));
			
			// Save the METARs
			log.info("Saving METAR cycle - {} entries", Integer.valueOf(data.size()));
			wxwdao.writeMETAR(data.values());
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.atError().withThrowable(de).log("Error saving METAR Data - {}", de.getMessage());
		} finally {
			ctx.release();
		}
	}
}