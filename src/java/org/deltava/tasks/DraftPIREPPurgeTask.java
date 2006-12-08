// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.FlightReport;

import org.deltava.dao.*;

import org.deltava.taskman.DatabaseTask;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to purge Draft Flight Reports from the database.
 * @author Luke
 * @version 1.0
 * @since
 */

public class DraftPIREPPurgeTask extends DatabaseTask {

	private static final Integer[] DRAFT = { new Integer(FlightReport.DRAFT) };

	/**
	 * Initializes the Task.
	 */
	public DraftPIREPPurgeTask() {
		super("Draft PIREP Purge", DraftPIREPPurgeTask.class);
	}

	/**
	 * Executes the Task.
	 */
	protected void execute() {

		// Determine how many days to purge
		int purgeDays = SystemData.getInt("users.pirep.draft_purge", 30);
		Calendar cld = Calendar.getInstance();
		cld.add(Calendar.DAY_OF_MONTH, (purgeDays * -1));
		log.info("Purging draft Flight Reports before " + cld.getTime());

		try {
			Connection con = getConnection();
			
			// Get the DAO and the Flight Reports - remove based on date
			GetFlightReports dao = new GetFlightReports(con);
			Collection<FlightReport> pireps = dao.getByStatus(Arrays.asList(DRAFT));
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
			release();
		}

		// Log completion
		log.info("Processing Complete");
	}
}