// Copyright 2006, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.flight.FlightReport;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to purge Draft Flight Reports from the database.
 * @author Luke
 * @version 2.7
 * @since 1.0
 */

public class DraftPIREPPurgeTask extends Task {

	private static final Collection<Integer> DRAFT = Collections.singleton(new Integer(FlightReport.DRAFT));

	/**
	 * Initializes the Task.
	 */
	public DraftPIREPPurgeTask() {
		super("Draft PIREP Purge", DraftPIREPPurgeTask.class);
	}

	/**
	 * Executes the Task.
	 */
	protected void execute(TaskContext ctx) {

		// Determine how many days to purge
		int purgeDays = SystemData.getInt("users.pirep.draft_purge", 30);
		Calendar cld = Calendar.getInstance();
		cld.add(Calendar.DAY_OF_MONTH, (purgeDays * -1));
		log.info("Purging draft Flight Reports before " + cld.getTime());

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Flight Reports - remove based on date
			GetFlightReports dao = new GetFlightReports(con);
			Collection<FlightReport> pireps = dao.getByStatus(DRAFT);
			for (Iterator<FlightReport> i = pireps.iterator(); i.hasNext();) {
				FlightReport fr = i.next();
				if (fr.getDate().after(cld.getTime()))
					i.remove();
			}

			// Get the write DAO and purge
			SetFlightReport wdao = new SetFlightReport(con);
			for (Iterator<FlightReport> i = pireps.iterator(); i.hasNext();) {
				FlightReport fr = i.next();
				log.info("Deleting flight " + fr.getFlightCode() + " Date=" + fr.getDate());
				wdao.delete(fr.getID());
			}
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}

		// Log completion
		log.info("Processing Complete");
	}
}