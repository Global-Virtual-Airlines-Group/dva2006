// Copyright 2006, 2007, 2009, 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.util.stream.Collectors;
import java.time.*;
import java.sql.Connection;

import org.deltava.beans.flight.FlightReport;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to purge Draft Flight Reports from the database.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class DraftPIREPPurgeTask extends Task {

	private static final Collection<Integer> DRAFT = Collections.singleton(Integer.valueOf(FlightReport.DRAFT));

	/**
	 * Initializes the Task.
	 */
	public DraftPIREPPurgeTask() {
		super("Draft PIREP Purge", DraftPIREPPurgeTask.class);
	}

	/**
	 * Executes the Task.
	 */
	@Override
	protected void execute(TaskContext ctx) {

		// Determine how many days to purge
		int purgeDays = SystemData.getInt("users.pirep.draft_purge", 30);
		Instant pd = ZonedDateTime.now(ZoneOffset.UTC).minusDays(purgeDays).toInstant();
		log.warn("Purging draft Flight Reports before " + pd);

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Flight Reports - remove based on date
			GetFlightReports dao = new GetFlightReports(con);
			Collection<FlightReport> pireps = dao.getByStatus(DRAFT).stream().filter(fr -> fr.getDate().isBefore(pd)).collect(Collectors.toList());

			// Get the write DAO and purge
			SetFlightReport wdao = new SetFlightReport(con);
			for (FlightReport fr : pireps) {
				log.warn("Deleting flight " + fr.getFlightCode() + " Date=" + fr.getDate());
				wdao.delete(fr.getID());
			}
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}

		log.info("Processing Complete");
	}
}