// Copyright 2009, 2016, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.time.ZonedDateTime;

import org.deltava.beans.wx.*;

import org.deltava.dao.*;
import org.deltava.dao.http.*;

import org.deltava.taskman.*;
import org.deltava.util.StringUtils;

/**
 * A Scheduled Task to download TAF data.
 * @author Luke
 * @version 10.3
 * @since 2.7
 */

public class TAFDownloadTask extends Task {

	/**
	 * Initializes the Task.
	 */
	public TAFDownloadTask() {
		super("TAF Download", TAFDownloadTask.class);
	}

	@Override
	protected void execute(TaskContext ctx) {
		int hour = ZonedDateTime.now().getHour();
		hour -= (hour % 6);
		try {
			GetNOAAWeather wxdao = new GetNOAAWeather();
			wxdao.setCompression(Compression.GZIP, Compression.BROTLI);
			log.info("Loading TAF cycle for " + StringUtils.format(hour, "00") + "00Z");
			Map<String, TAF> tafs = wxdao.getTAFCycle(hour);
			
			// Get the DAO
			SetWeather wxwdao = new SetWeather(ctx.getConnection());
			ctx.startTX();
			
			// Purge the data
			wxwdao.purgeTAF(490);
			
			// Save the TAFs
			log.info("Saving TAF cycle - " + tafs.size() + " entries");
			for (TAF t : tafs.values())
				wxwdao.write(t);
			
			// Commit
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.error("Error saving TAF Data - " + de.getMessage(), de);
		} finally {
			ctx.release();
		}
	}
}