// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.time.LocalDate;

import org.deltava.beans.schedule.ScheduleSource;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.StringUtils;

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
		
		// Get yesterday
		LocalDate ed = LocalDate.now().minusDays(1);
		
		// Extend the date
		try {
			SetSchedule wdao = new SetSchedule(ctx.getConnection());
			int entryCount = wdao.extendRaw(ScheduleSource.AVSTACK, ed);
			log.info("Extended {} schedule entries to {}", Integer.valueOf(entryCount), StringUtils.format(LocalDate.now(), "MM/dd/yyyy"));
		} catch (DAOException de) {
			log.atError().withThrowable(de).log(de.getMessage());
		} finally {
			ctx.release();
		}

		log.info("Complete");
	}
}