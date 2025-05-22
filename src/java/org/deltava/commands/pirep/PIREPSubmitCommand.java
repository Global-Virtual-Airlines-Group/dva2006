// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.time.*;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.apache.logging.log4j.*;

import org.deltava.beans.*;
import org.deltava.beans.econ.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.*;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle Fligt Report submissions.
 * @author Luke
 * @version 12.0
 * @since 1.0
 */

public class PIREPSubmitCommand extends AbstractCommand {
	
	private static final Logger log = LogManager.getLogger(PIREPSubmitCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();

			// Get the PIREP to submit
			final int id = ctx.getID();
			GetFlightReports frdao = new GetFlightReports(con);
			FlightReport pirep = frdao.get(id, ctx.getDB());
			if (pirep == null)
				throw notFoundException(String.format("Invalid Flight Report - %d", Integer.valueOf(id)));

			// Check our access level
			PIREPAccessControl access = new PIREPAccessControl(ctx, pirep);
			access.validate();
			if (!access.getCanSubmit())
				throw securityException("Cannot submit Flight Report #" + pirep.getID());

			// Get the Pilot profile of the individual who flew this flight
			GetPilot pdao = new GetPilot(con);
			CacheManager.invalidate("Pilots", Integer.valueOf(pirep.getDatabaseID(DatabaseID.PILOT)));
			Pilot p = pdao.get(pirep.getDatabaseID(DatabaseID.PILOT));
			
			// Get the submission helper
			FlightSubmissionHelper fsh = new FlightSubmissionHelper(con, pirep, p);
			fsh.setAirlineInfo(SystemData.get("airline.code"), ctx.getDB());
			Collection<GeospaceLocation> rte = GeoUtils.greatCircle(pirep.getAirportD(), pirep.getAirportA(), GeoUtils.GC_SEGMENT_SIZE).stream().map(GeoPosition::new).collect(Collectors.toList());
			fsh.addPositions(rte);
			
			// If we found a draft flight report, save its database ID and copy its ID to the PIREP we will file
			boolean isReplace = fsh.checkFlightReports();
			
			// Check ETOPS on route
			if (StringUtils.isEmpty(pirep.getRoute())) {
				GetNavRoute nddao = new GetNavRoute(con);
				nddao.setEffectiveDate(pirep.getDate());
				RouteBuilder rb = new RouteBuilder(pirep, pirep.getRoute());
				nddao.getRouteWaypoints(rb.getRoute(), rb.getAirportD()).forEach(rb::add); // ignore sid/star for ETOPS calculations
				ETOPSResult etops = ETOPSHelper.classify(GeoUtils.stripDetours(rb.getPoints(), 65));
				
				// Get aircraft data
				GetAircraft acdao = new GetAircraft(con);
				Aircraft a = acdao.get(pirep.getEquipmentType());
				AircraftPolicyOptions opts = (a == null) ? null : a.getOptions(SystemData.get("airline.code"));
				if (opts != null) {
					pirep.setAttribute(FlightReport.ATTR_ETOPSWARN, (etops.getResult().getTime() > opts.getETOPS().getTime()));
					if (pirep.hasAttribute(FlightReport.ATTR_ETOPSWARN))
						pirep.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Filed route is %s, aircraft only rated for %s", etops.getResult(), opts.getETOPS()));
					else if (etops.getResult().getTime() > 75)
						pirep.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Filed route is %s", etops.getResult()));
				}
			}
			
			// Submitted!
			pirep.setStatus(FlightStatus.SUBMITTED);
			pirep.setSubmittedOn(Instant.now());
			pirep.addStatusUpdate(ctx.getUser().getID(), HistoryType.LIFECYCLE, "Submitted manually via web site");

			// Get our equipment program
			GetEquipmentType eqdao = new GetEquipmentType(con);
			ctx.setAttribute("eqType", eqdao.get(p.getEquipmentType()), REQUEST);
			
			// Check if the pilot is rated in the equipment type
			fsh.checkRatings();
			fsh.checkAircraft();
			
			// Check Online status, and Online Event
			fsh.checkOnlineNetwork();
			fsh.checkOnlineEvent();
			
			// Check the airspace / ETOPS
			fsh.checkAirspace();
			
			// Check for a Flight Tour
			fsh.checkTour();
			
			// Calculate the load factor
			fsh.calculateLoadFactor((EconomyInfo) SystemData.getObject(SystemData.ECON_DATA));

			// Check the schedule database and check the route pair
			Duration avgTime = fsh.checkSchedule();
			if (pirep.hasAttribute(FlightReport.ATTR_TIMEWARN))
				ctx.setAttribute("avgTime", avgTime, REQUEST);
			
			// Start transaction
			ctx.startTX();
			
			// If we need to replace, do that
			SetFlightReport fwdao = new SetFlightReport(con);
			if (isReplace) {
				List<FlightReport> dFlights = frdao.getDraftReports(p.getID(), pirep, ctx.getDB());
				dFlights.removeIf(dfr -> dfr.getID() == id);
				if (!dFlights.isEmpty()) {
					int draftID = dFlights.getFirst().getID();
					pirep.addStatusUpdate(0, HistoryType.SYSTEM, String.format("Replaced existing draft Flight Report %d", Integer.valueOf(draftID)));
					fwdao.delete(draftID);
				}
			}

			// Write the PIREP to the database
			fwdao.write(pirep, ctx.getDB());
			if (fwdao.updatePaxCount(pirep.getID()))
				log.warn("Update Passenger count for PIREP #{}", Integer.valueOf(pirep.getID()));
			
			// Move track data from the raw table
			if (fsh.hasTrackData()) {
				SetOnlineTrack twdao = new SetOnlineTrack(con);	
				twdao.write(pirep.getID(), fsh.getTrackData(), ctx.getDB());
				twdao.purgeRaw(fsh.getTrackID());
			}
			
			// If we've updated the Flight Report based on an old flight report, delete it
			if (id != pirep.getID())
				fwdao.delete(id);
			
			// Save the pirep in the request
			ctx.commitTX();
			ctx.setAttribute("pirep", pirep, REQUEST);
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("isSubmitted", Boolean.TRUE, REQUEST);
			ctx.setAttribute("notRated", Boolean.valueOf(pirep.hasAttribute(FlightReport.ATTR_NOTRATED)), REQUEST);
			ctx.setAttribute("isOurs", Boolean.valueOf(pirep.getDatabaseID(DatabaseID.PILOT) == ctx.getUser().getID()), REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/pilot/pirepUpdate.jsp");
		result.setSuccess(true);
	}
}