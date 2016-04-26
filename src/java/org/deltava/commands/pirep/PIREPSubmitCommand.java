// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.academy.*;
import org.deltava.beans.econ.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.event.Event;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.*;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle Fligt Report submissions.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class PIREPSubmitCommand extends AbstractCommand {
	
	private static final Logger log = Logger.getLogger(PIREPSubmitCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();

			// Get the PIREP to submit
			GetFlightReports frdao = new GetFlightReports(con);
			FlightReport pirep = frdao.get(ctx.getID());
			if (pirep == null)
				throw notFoundException("Flight Report Not Found");

			// Check our access level
			PIREPAccessControl access = new PIREPAccessControl(ctx, pirep);
			access.validate();
			if (!access.getCanSubmit())
				throw securityException("Cannot submit Flight Report #" + pirep.getID());

			// Get the Pilot profile of the individual who flew this flight
			GetPilot pdao = new GetPilot(con);
			CacheManager.invalidate("Pilots", Integer.valueOf(pirep.getDatabaseID(DatabaseID.PILOT)));
			Pilot p = pdao.get(pirep.getDatabaseID(DatabaseID.PILOT));
			
			// Create comments field
			Collection<String> comments = new ArrayList<String>();
			
			// If we found a draft flight report, save its database ID and copy its ID to the PIREP we will file
			List<FlightReport> dFlights = frdao.getDraftReports(p.getID(), pirep, SystemData.get("airline.db"));
			if (!dFlights.isEmpty()) {
				FlightReport fr = dFlights.get(0);
				if (pirep.getID() == 0)
					pirep.setID(fr.getID());
				pirep.setDatabaseID(DatabaseID.ASSIGN, fr.getDatabaseID(DatabaseID.ASSIGN));
				pirep.setDatabaseID(DatabaseID.EVENT, fr.getDatabaseID(DatabaseID.EVENT));
				pirep.setAttribute(FlightReport.ATTR_CHARTER, fr.hasAttribute(FlightReport.ATTR_CHARTER));
				if (!StringUtils.isEmpty(fr.getComments()))
					comments.add(fr.getComments());
			}
			
			// Submitted!
			pirep.setSubmittedOn(Instant.now());

			// Save the Pilot profile
			ctx.setAttribute("pilot", p, REQUEST);

			// Get our equipment program
			GetEquipmentType eqdao = new GetEquipmentType(con);
			EquipmentType eq = eqdao.get(p.getEquipmentType());
			ctx.setAttribute("eqType", eq, REQUEST);
			
			// Check if the pilot is rated in the equipment type
			Collection<String> allRatings = new HashSet<String>(p.getRatings());
			allRatings.addAll(eq.getRatings());
			boolean isRated = allRatings.contains(pirep.getEquipmentType());
			ctx.setAttribute("notRated", Boolean.valueOf(!isRated), REQUEST);
			pirep.setAttribute(FlightReport.ATTR_NOTRATED, !isRated);
			if (!isRated)
				log.warn(p.getName() + " not rated in " + pirep.getEquipmentType() + " ratings = " + p.getRatings());

			// Check if this flight was flown with an equipment type in our primary ratings
			Collection<String> pTypeNames = eqdao.getPrimaryTypes(SystemData.get("airline.db"), pirep.getEquipmentType());
			if (pTypeNames.contains(p.getEquipmentType())) {
				FlightPromotionHelper helper = new FlightPromotionHelper(pirep);
				for (Iterator<String> i = pTypeNames.iterator(); i.hasNext(); ) {
					String pType = i.next();
					EquipmentType pEQ = eqdao.get(pType, SystemData.get("airline.db"));
					if (!helper.canPromote(pEQ)) {
						i.remove();
						if (!StringUtils.isEmpty(helper.getLastComment()))
							comments.add("Not eligible for promotion: " + helper.getLastComment());
					}
				}
				
				// Add programs if we still have any that do not require ACARS legs
				if (!pTypeNames.isEmpty()) {
					ctx.setAttribute("captEQ", Boolean.TRUE, REQUEST);
					ctx.setAttribute("promoteLegs", Integer.valueOf(eq.getPromotionLegs()), REQUEST);
					pirep.setCaptEQType(pTypeNames);
				}
			}
			
			// Check if it's a Flight Academy flight
			GetSchedule sdao = new GetSchedule(con);
			ScheduleEntry sEntry = sdao.get(pirep);
			boolean isAcademy = ((sEntry != null) && sEntry.getAcademy());
			if (isAcademy) {
				GetAcademyCourses crsdao = new GetAcademyCourses(con);
				Collection<Course> courses = crsdao.getByPilot(p.getID());
				for (Course c : courses) {
					if (c.getStatus() == Status.STARTED) {
						pirep.setAttribute(FlightReport.ATTR_ACADEMY, true);
						break;
					}
				}
			}
			
			// Check if it's an Online Event flight
			GetEvent evdao = new GetEvent(con);
			if ((pirep.getDatabaseID(DatabaseID.EVENT) == 0) && (pirep.hasAttribute(FlightReport.ATTR_ONLINE_MASK))) {
				int eventID = evdao.getPossibleEvent(pirep);
				if (eventID != 0) {
					Event e = evdao.get(eventID);
					comments.add("SYSTEM: Detected participation in " + e.getName() + " Online Event");
					pirep.setDatabaseID(DatabaseID.EVENT, eventID);
				}
			}
			
			// Check that the event hasn't expired
			if (pirep.getDatabaseID(DatabaseID.EVENT) != 0) {
				Event e = evdao.get(pirep.getDatabaseID(DatabaseID.EVENT));
				if (e != null) {
					long timeSinceEnd = (System.currentTimeMillis() - e.getEndTime().toEpochMilli()) / 3600_000;
					if (timeSinceEnd > 24) {
						comments.add("SYSTEM: Flight logged " + timeSinceEnd + " hours after '" + e.getName() + "' completion");
						pirep.setDatabaseID(DatabaseID.EVENT, 0);
					}
				} else
					pirep.setDatabaseID(DatabaseID.EVENT, 0);
			}

			// Check the range
			GetAircraft acdao = new GetAircraft(con);
			Aircraft a = acdao.get(pirep.getEquipmentType());
			if (pirep.getDistance() > a.getRange()) {
				pirep.setAttribute(FlightReport.ATTR_RANGEWARN, true);
				ctx.setAttribute("rangeWarning", Boolean.TRUE, REQUEST);
			}
			
			// Check ETOPS
			Collection<GeoLocation> gc = GeoUtils.greatCircle(pirep.getAirportD(), pirep.getAirportA(), 25);
			ETOPSResult er = ETOPSHelper.classify(gc);
			pirep.setAttribute(FlightReport.ATTR_ETOPSWARN, ETOPSHelper.validate(a, er.getResult()));
			if (pirep.hasAttribute(FlightReport.ATTR_ETOPSWARN))
				comments.add("ETOPS classificataion: " + String.valueOf(er));
			
			// Calculate the load factor
			EconomyInfo eInfo = (EconomyInfo) SystemData.getObject(SystemData.ECON_DATA);
			if (eInfo != null) {
				LoadFactor lf = new LoadFactor(eInfo);
				double loadFactor = lf.generate(pirep.getSubmittedOn());
				pirep.setPassengers((int) Math.round(a.getSeats() * loadFactor));
				pirep.setLoadFactor(loadFactor);
			}

			// Check the schedule database and check the route pair
			boolean isAssignment = (pirep.getDatabaseID(DatabaseID.ASSIGN) != 0);
			FlightTime avgHours = sdao.getFlightTime(pirep);
			if (!avgHours.hasCurrent() && !avgHours.hasHistoric() && !isAcademy && !isAssignment) {
				pirep.setAttribute(FlightReport.ATTR_ROUTEWARN, true);
				ctx.setAttribute("unknownRoute", Boolean.TRUE, REQUEST);
			} else {
				int minHours = (int) ((avgHours.getFlightTime() * 0.75) - (SystemData.getDouble("users.pirep.pad_hours", 0) * 10));
				int maxHours = (int) ((avgHours.getFlightTime() * 1.15) + (SystemData.getDouble("users.pirep.pad_hours", 0) * 10));

				if ((pirep.getLength() < minHours) || (pirep.getLength() > maxHours)) {
					pirep.setAttribute(FlightReport.ATTR_TIMEWARN, true);
					ctx.setAttribute("timeWarning", Boolean.TRUE, REQUEST);
					ctx.setAttribute("avgTime", Integer.valueOf(avgHours.getFlightTime()), REQUEST);
				}
			}
			
			// Update the status of the PIREP
			pirep.setStatus(FlightReport.SUBMITTED);
			if (!comments.isEmpty())
				pirep.setComments(StringUtils.listConcat(comments, "\r\n"));

			// Get the DAO and write the PIREP to the database
			SetFlightReport fwdao = new SetFlightReport(con);
			fwdao.write(pirep);
			if (fwdao.updatePaxCount(pirep.getID(), SystemData.get("airline.db")))
				log.warn("Update Passnger count for PIREP #" + pirep.getID());
			
			// Save the pirep in the request
			ctx.setAttribute("pirep", pirep, REQUEST);
			ctx.setAttribute("isOurs", Boolean.valueOf(pirep.getDatabaseID(DatabaseID.PILOT) == ctx.getUser().getID()), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Set status for JSP
		ctx.setAttribute("isSubmitted", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/pilot/pirepUpdate.jsp");
		result.setSuccess(true);
	}
}