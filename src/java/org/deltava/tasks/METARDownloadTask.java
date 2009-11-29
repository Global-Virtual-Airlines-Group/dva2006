// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;

import org.deltava.beans.wx.*;

import org.deltava.dao.*;
import org.deltava.dao.file.GetNOAAWeather;

import org.deltava.taskman.*;
import org.deltava.util.StringUtils;

/**
 * A Scheduled Task to download METAR data.
 * @author Luke
 * @version 2.7
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
		
		Calendar cld = Calendar.getInstance();
		int hour = cld.get(Calendar.HOUR_OF_DAY);
		try {
			GetNOAAWeather wxdao = new GetNOAAWeather();
			log.info("Loading METAR cycle for " + StringUtils.format(hour, "00") + "00Z");
			Map<String, METAR> metars = wxdao.getMETARCycle(hour);
			
			// Get the DAO
			SetWeather wxwdao = new SetWeather(ctx.getConnection());
			ctx.startTX();
			
			// Purge the data
			wxwdao.purgeMETAR(90);
			
			// Save the METARs
			log.info("Saving METAR cycle - " + metars.size() + " entries");
			for (Iterator<METAR> i = metars.values().iterator(); i.hasNext(); ) {
				METAR m = i.next();
				wxwdao.write(m);
			}
			
			// Commit
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.error("Error saving METAR Data - " + de.getMessage(), de);
		} finally {
			ctx.release();
		}
	}
}