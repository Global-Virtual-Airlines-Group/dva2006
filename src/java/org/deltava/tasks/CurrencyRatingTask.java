// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.testing.*;

import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to update Pilot ratings based on currency Check Rides.
 * @author Luke
 * @version 8.0
 * @since 8.0
 */

public class CurrencyRatingTask extends Task {

	/**
	 * Initializes the Task.
	 */
	public CurrencyRatingTask() {
		super("Currency Rating Update", CurrencyRatingTask.class);
	}

	/**
	 * Executes the Task.
	 */
	@Override
	protected void execute(TaskContext ctx) {
		
		int currencyDays = SystemData.getInt("testing.currency.validity", 365);
		try {
			Connection con = ctx.getConnection();
			
			// Load Pilots enrolled in program
			GetPilot pdao = new GetPilot(con);
			Collection<Pilot> pilots = pdao.getCurrencyPilots();
			
			// Load their check rides, and determine what has expired
			GetExam exdao = new GetExam(con);
			GetFlightReports frdao = new GetFlightReports(con);
			GetEquipmentType eqdao = new GetEquipmentType(con);
			for (Pilot p : pilots) {
				Collection<FlightReport> pireps = frdao.getByPilot(p.getID(), null);
				frdao.getCaptEQType(pireps);
				
				// Load flights and exams for pilot
				TestingHistoryHelper helper = new TestingHistoryHelper(p, eqdao.get(p.getEquipmentType()), exdao.getExams(p.getID()), pireps);
				helper.applyExpiration(currencyDays);
				
				// Go back and rebuild the list of things we are eligible for
				Collection<String> newRatings = new TreeSet<String>();
				Collection<EquipmentType> newEQ = eqdao.getActive();
				for (Iterator<EquipmentType> i = newEQ.iterator(); i.hasNext(); ) {
					EquipmentType eq = i.next();
					try {
						helper.canSwitchTo(eq);
						newRatings.addAll(eq.getRatings());
					} catch (IneligibilityException ie) {
						i.remove();
					}
				}

				// If newEQ is empty, disable currency ratings
				
				// If newEQ is not empty but doesn't include eqType, switch to one that does
				
				
				
			}
			
			
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}

		log.info("Processing Complete");
	}
}