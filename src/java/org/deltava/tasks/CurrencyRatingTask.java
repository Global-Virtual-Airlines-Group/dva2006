// Copyright 2017, 2019, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.system.MessageTemplate;
import org.deltava.beans.testing.*;

import org.deltava.dao.*;
import org.deltava.mail.*;
import org.deltava.taskman.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to update Pilot ratings based on currency Check Rides.
 * @author Luke
 * @version 10.0
 * @since 8.0
 */

public class CurrencyRatingTask extends Task {

	/**
	 * Initializes the Task.
	 */
	public CurrencyRatingTask() {
		super("Currency Rating Update", CurrencyRatingTask.class);
	}

	@Override
	protected void execute(TaskContext ctx) {
		
		int currencyDays = SystemData.getInt("testing.currency.validity", 365);
		int expDays = Math.min(30, Math.max(15, SystemData.getInt("testing.currency.validity", 365)));
		try {
			Connection con = ctx.getConnection();
			
			// Load Pilots enrolled in program
			GetPilot pdao = new GetPilot(con);
			Collection<Pilot> pilots = pdao.getCurrencyPilots();
			log.info("Recaculating ratings for " + pilots.size() + " Pilots");
			
			// Get all equipment types
			GetEquipmentType eqdao = new GetEquipmentType(con);
			Collection<EquipmentType> allEQ = eqdao.getActive();
			
			// Load message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			MessageTemplate mt = mtdao.get("CURREXPIRING");
			
			// Load their check rides, and determine what has expired
			GetExam exdao = new GetExam(con);
			GetFlightReports frdao = new GetFlightReports(con);
			SetPilot pwdao = new SetPilot(con);
			SetStatusUpdate suwdao = new SetStatusUpdate(con);
			for (Pilot p : pilots) {
				Collection<FlightReport> pireps = frdao.getByPilot(p.getID(), null);
				frdao.getCaptEQType(pireps);
				EquipmentType myEQ = eqdao.get(p.getEquipmentType());
				boolean noUpdate = false;
				
				// Load flights and exams for pilot
				TestingHistoryHelper helper = new TestingHistoryHelper(p, myEQ, exdao.getExams(p.getID()), pireps);
				helper.setEquipmentTypes(allEQ);
				helper.applyExpiration(currencyDays);
				
				// Go back and rebuild the list of things we are eligible for
				Collection<String> newRatings = helper.getQualifiedRatings();
				SortedSet<EquipmentType> newEQ = helper.getQualifiedPrograms();

				// If newEQ is empty, disable currency ratings
				Collection<StatusUpdate> upds = new ArrayList<StatusUpdate>();
				if (newEQ.isEmpty()) {
					p.setProficiencyCheckRides(false);
					StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.CURRENCY);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription("Proficiency Check Rides disabled, no current ratings");
					upds.add(upd);
					log.info(p.getName() + " " + upd.getDescription());
					
					// Recalculate ratings
					helper.clearExpiration();
					newRatings = helper.getQualifiedRatings();
					
				} else if (!newEQ.contains(myEQ)) { // If newEQ is not empty but doesn't include eqType, switch to one that does
					EquipmentType newET = newEQ.last();
					p.setEquipmentType(newET.getName());
					Collection<String> removedRatings = CollectionUtils.getDelta(p.getRatings(), newRatings);
					log.info(p.getName() + " lost " + myEQ + " rating, switching to " + newET);
					log.info(p.getName() + " removed " + removedRatings + " ratings");
					p.removeRatings(removedRatings);
					
					StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.CURRENCY);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription("Lost rating in " + myEQ.getName() + ", switching to " + newET.getName());
					upds.add(upd);
					
					upd = new StatusUpdate(p.getID(), UpdateType.RATING_REMOVE);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription("Ratings removed: " + StringUtils.listConcat(removedRatings, ", "));
					upds.add(upd);
				} else if (CollectionUtils.hasDelta(p.getRatings(), newRatings)) { // Check if any ratings were removed
					Collection<String> removedRatings = CollectionUtils.getDelta(p.getRatings(), newRatings);
					p.removeRatings(removedRatings);
					log.info(p.getName() + " removed " + removedRatings + " ratings");
					
					StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.RATING_REMOVE);
					upd.setAuthorID(ctx.getUser().getID());
					upd.setDescription("Ratings removed: " + StringUtils.listConcat(removedRatings, ", "));
					upds.add(upd);
				} else
					noUpdate = true;
				
				// Check if any ratings are expiring
				Collection<CheckRide> expRides = helper.getCheckRides(expDays);
				if (!expRides.isEmpty()) {
					Collection<String> rideNames = expRides.stream().map(CheckRide::getName).collect(Collectors.toList());
					MessageContext mctxt = new MessageContext();
					mctxt.addData("exams", StringUtils.listConcat(rideNames, ", "));
					mctxt.setTemplate(mt);
					Mailer m = new Mailer(ctx.getUser());
					m.send(p);
				}
				
				if (!noUpdate) {
					ctx.startTX();
					pwdao.write(p, ctx.getDB());
					suwdao.write(upds);
					ctx.commitTX();
				}
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}

		log.info("Processing Complete");
	}
}