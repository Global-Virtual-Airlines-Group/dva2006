// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.Collection;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to extend the validity of a day's AviationStack schedule entries. 
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class AVStackExtendTask extends Task {

	/**
	 * Creates the Scheduled Task.
	 */
	public AVStackExtendTask() {
		super("AviationStack Extend", AVStackExtendTask.class);
	}

	@Override
	protected void execute(TaskContext ctx) {
		
		// Get download window size
		int daysFwd = SystemData.getInt("schedule.avstack.days", 14);
		LocalDate effDate = LocalDate.now().plusDays(daysFwd);
		
		try {
			Connection con = ctx.getConnection();
			
			// See if we have flights for that day
			GetRawSchedule rsdao = new GetRawSchedule(con);
			Collection<RawScheduleEntry> entries = rsdao.load(ScheduleSource.AVSTACK, effDate);
			if (entries.isEmpty()) {
				log.warn("No AviationStack fligts found for {}, extending {} by 1 day", StringUtils.format(effDate, "MM/dd/yyyy"), StringUtils.format(effDate.minusDays(1), "MM/dd/yyyy"));
				
				// Extend by a day
				SetSchedule wdao = new SetSchedule(con);	
				int entryCount = wdao.extendRaw(ScheduleSource.AVSTACK, effDate.minusDays(1));
				log.info("Extended {} raw schedule entries to {}", Integer.valueOf(entryCount), StringUtils.format(effDate, "MM/dd/yyyy"));
			} else
				log.info("Found {} AviationStack flights for {}", Integer.valueOf(entries.size()), StringUtils.format(effDate, "MM/dd/yyyy"));
		} catch (DAOException de) {
			log.atError().withThrowable(de).log(de.getMessage());
		} finally {
			ctx.release();
		}

		log.info("Complete");
	}
}