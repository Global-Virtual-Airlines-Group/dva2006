// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2015, 2016, 2017, 2018, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.academy.*;
import org.deltava.beans.econ.*;
import org.deltava.beans.event.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.servinfo.PositionData;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.*;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle Fligt Report submissions.
 * @author Luke
 * @version 9.0
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
					pirep.setComments(fr.getComments());
			}
			
			// Submitted!
			pirep.setSubmittedOn(Instant.now());
			pirep.addStatusUpdate(ctx.getUser().getID(), HistoryType.LIFECYCLE, "Submitted manually via web site");

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
				pirep.addStatusUpdate(0, HistoryType.SYSTEM, p.getName() + " not rated in " + pirep.getEquipmentType() + " ratings = " + p.getRatings());

			// Check if this flight was flown with an equipment type in our primary ratings
			Collection<String> pTypeNames = eqdao.getPrimaryTypes(SystemData.get("airline.db"), pirep.getEquipmentType());
			if (pTypeNames.contains(p.getEquipmentType())) {
				FlightPromotionHelper helper = new FlightPromotionHelper(pirep);
				for (Iterator<String> i = pTypeNames.iterator(); i.hasNext(); ) {
					String pType = i.next();
					EquipmentType pEQ = eqdao.get(pType, SystemData.get("airline.db"));
					if (!helper.canPromote(pEQ)) {
						i.remove();
						pirep.addStatusUpdate(0, HistoryType.SYSTEM, "Not eligible for promotion: " + helper.getLastComment());
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
			GetRawSchedule rsdao = new GetRawSchedule(con);
			GetSchedule sdao = new GetSchedule(con);
			sdao.setSources(rsdao.getSources(true));
			ScheduleEntry sEntry = sdao.get(pirep);
			boolean isAcademy = ((sEntry != null) && sEntry.getAcademy());
			if (isAcademy) {
				GetAcademyCourses crsdao = new GetAcademyCourses(con);
				Collection<Course> courses = crsdao.getByPilot(p.getID());
				Course c = courses.stream().filter(crs -> (crs.getStatus() == org.deltava.beans.academy.Status.STARTED)).findAny().orElse(null);
				pirep.setAttribute(FlightReport.ATTR_ACADEMY, (c != null));
			}
			
			// If it's online load the track data
			Collection<PositionData> pd = new ArrayList<PositionData>(); int trackID = 0;
			if (pirep.hasAttribute(FlightReport.ATTR_ONLINE_MASK)) {
				GetOnlineTrack tdao = new GetOnlineTrack(con); 
				trackID = tdao.getTrackID(pirep.getDatabaseID(DatabaseID.PILOT), pirep.getNetwork(), pirep.getSubmittedOn(), pirep.getAirportD(), pirep.getAirportA());
				if (trackID != 0) {
					pd.addAll(tdao.getRaw(trackID));
					if (!pd.isEmpty())
						pirep.addStatusUpdate(0, HistoryType.SYSTEM, "Loaded " + pirep.getNetwork() + " online track data (" + pd.size() + " positions)");
					if (StringUtils.isEmpty(pirep.getRoute())) {
						pirep.setRoute(tdao.getRoute(trackID));
						pirep.addStatusUpdate(0, HistoryType.SYSTEM, "Updated route from " + pirep.getNetwork() + " flight plan");
					}
				}
			}
			
			// Check if it's an Online Event flight
			GetEvent evdao = new GetEvent(con);
			EventFlightHelper efr = new EventFlightHelper(pirep);
			efr.addOnlineTrack(pd);
			if ((pirep.getDatabaseID(DatabaseID.EVENT) == 0) && (pirep.hasAttribute(FlightReport.ATTR_ONLINE_MASK))) {
				List<Event> events = evdao.getPossibleEvents(pirep, SystemData.get("airline.code"));
				events.removeIf(e -> !efr.matches(e));
				if (!events.isEmpty()) {
					Event e = events.get(0);
					pirep.addStatusUpdate(0, HistoryType.SYSTEM, "Detected participation in " + e.getName() + " Online Event");
					pirep.setDatabaseID(DatabaseID.EVENT, e.getID());
				}
			}
			
			// Check that the event hasn't expired
			if (pirep.getDatabaseID(DatabaseID.EVENT) != 0) {
				Event e = evdao.get(pirep.getDatabaseID(DatabaseID.EVENT));
				if ((e != null) && !efr.matches(e)) {
					pirep.addStatusUpdate(0, HistoryType.SYSTEM, efr.getMessage());
					pirep.setDatabaseID(DatabaseID.EVENT, 0);
				} else {
					pirep.addStatusUpdate(0, HistoryType.SYSTEM, "Unknown Online Event - " + pirep.getDatabaseID(DatabaseID.EVENT));
					pirep.setDatabaseID(DatabaseID.EVENT, 0);
				}
			}

			// Check the range
			GetAircraft acdao = new GetAircraft(con);
			Aircraft a = acdao.get(pirep.getEquipmentType());
			AircraftPolicyOptions opts = a.getOptions(SystemData.get("airline.code"));
			if (pirep.getDistance() > opts.getRange()) {
				pirep.setAttribute(FlightReport.ATTR_RANGEWARN, true);
				ctx.setAttribute("rangeWarning", Boolean.TRUE, REQUEST);
			}
			
			// Check ETOPS
			Collection<GeoLocation> gc = GeoUtils.greatCircle(pirep.getAirportD(), pirep.getAirportA(), 25);
			ETOPSResult er = ETOPSHelper.classify(gc);
			pirep.setAttribute(FlightReport.ATTR_ETOPSWARN, ETOPSHelper.validate(opts, er.getResult()));
			if (pirep.hasAttribute(FlightReport.ATTR_ETOPSWARN))
				pirep.addStatusUpdate(0, HistoryType.SYSTEM, "ETOPS classificataion: " + String.valueOf(er));
			
			// Calculate the load factor
			EconomyInfo eInfo = (EconomyInfo) SystemData.getObject(SystemData.ECON_DATA);
			if (eInfo != null) {
				LoadFactor lf = new LoadFactor(eInfo);
				double loadFactor = lf.generate(pirep.getSubmittedOn());
				pirep.setPassengers((int) Math.round(opts.getSeats() * loadFactor));
				pirep.setLoadFactor(loadFactor);
			}

			// Check the schedule database and check the route pair
			boolean isAssignment = (pirep.getDatabaseID(DatabaseID.ASSIGN) != 0);
			FlightTime avgHours = sdao.getFlightTime(pirep);
			if ((avgHours.getType() == RoutePairType.UNKNOWN) && !isAcademy && !isAssignment) {
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
			pirep.setStatus(FlightStatus.SUBMITTED);
			
			// Start transaction
			ctx.startTX();

			// Get the DAO and write the PIREP to the database
			SetFlightReport fwdao = new SetFlightReport(con);
			fwdao.write(pirep);
			if (fwdao.updatePaxCount(pirep.getID()))
				log.warn("Update Passenger count for PIREP #" + pirep.getID());
			
			// Move track data from the raw table
			if (!pd.isEmpty()) {
				SetOnlineTrack twdao = new SetOnlineTrack(con);	
				twdao.write(pirep.getID(), pd, SystemData.get("airline.db"));
				twdao.purgeRaw(trackID);
			}
			
			// Save the pirep in the request
			ctx.commitTX();
			ctx.setAttribute("pirep", pirep, REQUEST);
			ctx.setAttribute("isOurs", Boolean.valueOf(pirep.getDatabaseID(DatabaseID.PILOT) == ctx.getUser().getID()), REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
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