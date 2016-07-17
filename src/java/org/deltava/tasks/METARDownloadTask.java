// Copyright 2009, 2011, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;
import java.time.ZonedDateTime;

import org.deltava.beans.navdata.AirportLocation;
import org.deltava.beans.wx.*;

import org.deltava.dao.*;
import org.deltava.dao.file.GetNOAAWeather;

import org.deltava.taskman.*;
import org.deltava.util.StringUtils;

/**
 * A Scheduled Task to download METAR data.
 * @author Luke
 * @version 7.0
 * @since 2.7
 */

public class METARDownloadTask extends Task {

	/**
	 * Initializes the Task.
	 */
	public METARDownloadTask() {
		super("METAR Download", METARDownloadTask.class);
	}

	/**
	 * Executes the Task.
	 */
	@Override
	protected void execute(TaskContext ctx) {
		
		int hour = ZonedDateTime.now().getHour();
		try {
			GetNOAAWeather wxdao = new GetNOAAWeather();
			log.info("Loading METAR cycle for " + StringUtils.format(hour, "00") + "00Z");
			Map<String, METAR> data = wxdao.getMETARCycle(hour);
			for (METAR m : data.values())
				m.setILS(WeatherUtils.getILS(m));
			
			// Get the DAOs
			Connection con = ctx.getConnection();
			GetNavData nddao = new GetNavData(con);
			SetWeather wxwdao = new SetWeather(con);
			ctx.startTX();
			
			// Purge the data
			wxwdao.purgeMETAR(270);
			
			// Save the METARs
			log.info("Saving METAR cycle - " + data.size() + " entries");
			for (METAR m : data.values()) {
				AirportLocation al = nddao.getAirport(m.getCode());
				if (al != null)
					m.setAirport(al);
					
				wxwdao.write(m);
			}
			
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.error("Error saving METAR Data - " + de.getMessage(), de);
		} finally {
			ctx.release();
		}
	}
}