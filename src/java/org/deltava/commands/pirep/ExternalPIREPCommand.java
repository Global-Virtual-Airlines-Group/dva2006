// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.acars.FlightInfo;
import org.deltava.beans.testing.CheckRide;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.security.command.*;

import org.deltava.util.*;

/**
 * A Web Site Command to allow cross-Airline Check Ride PIREPs to be viewed and evaluated.
 * @author Luke
 * @version 2.1
 * @since 2.0
 */

public class ExternalPIREPCommand extends AbstractCommand {
	
	// Check ride approval values
	private static final List<ComboAlias> crApprove = ComboUtils.fromArray(new String[] { "PASS", "UNSATISFACTORY" },
			new String[] { "true", "false" });

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the Check Ride
			GetExam exdao = new GetExam(con);
			CheckRide cr = exdao.getCheckRide(ctx.getID());
			if (cr == null)
				throw notFoundException("Invalid Check Ride ID - " + ctx.getID());
			
			// Load the pilot object
			GetUserData uddao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			UserData ud = uddao.get(cr.getPilotID());
			Pilot p = pdao.get(ud);
			if (p == null)
				throw notFoundException("Unknown Pilot ID - " + cr.getPilotID());
			
			// Validate our access
			ExamAccessControl crAccess = new ExamAccessControl(ctx, cr, ud);
			crAccess.validate();
			ctx.setAttribute("crAccess", crAccess, REQUEST);
			if (crAccess.getCanScore())
				ctx.setAttribute("crPassFail", crApprove, REQUEST);
			
			// Save the checkride
			ctx.setAttribute("checkRide", cr, REQUEST);
			
			// Get the Flight Report
			GetFlightReports frdao = new GetFlightReports(con);
			ACARSFlightReport fr = frdao.getACARS(ud.getDB(), cr.getFlightID());
			if (fr == null)
				throw notFoundException("Unknown Flight Report ID - " + cr.getFlightID());
			
			// Get the pilot who approved/rejected this PIREP
			int disposalID = fr.getDatabaseID(FlightReport.DBID_DISPOSAL);
			UserData dud = (disposalID == 0) ? null : uddao.get(disposalID);
			Pilot dPilot = pdao.get(dud);
			if (dPilot != null) {
				String msg = FlightReport.STATUS[fr.getStatus()] + " - by " + dPilot.getName();
				ctx.setAttribute("statusMsg", msg, REQUEST);
			} else
				ctx.setAttribute("statusMsg", FlightReport.STATUS[fr.getStatus()], REQUEST);
			
			// Get the pilot/PIREP beans in the request
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("pirep", fr, REQUEST);			
			
			// Create the access controller and stuff it in the request
			PIREPAccessControl ac = new CrossAppPIREPAccessControl(ctx, fr, cr);
			ac.validate();
			ctx.setAttribute("access", ac, REQUEST);
			ctx.setAttribute("scoreCR", Boolean.valueOf(crAccess.getCanScore()), REQUEST);
			
			// Get the route data from the DAFIF database
			GetACARSData ardao = new GetACARSData(con);
			FlightInfo info = ardao.getInfo(cr.getFlightID());
			if (info != null) {
				Collection<String> wps = new LinkedHashSet<String>();
				wps.add(info.getAirportD().getICAO());
				wps.addAll(StringUtils.split(info.getRoute(), " "));
				wps.add(info.getAirportA().getICAO());
				
				// Save ACARS info
				GetNavRoute navdao = new GetNavRoute(con);
				ctx.setAttribute("filedRoute", navdao.getRouteWaypoints(StringUtils.listConcat(wps, " "), info.getAirportD()), REQUEST);
				ctx.setAttribute("flightInfo", info, REQUEST);
				ctx.setAttribute("conInfo", ardao.getConnection(info.getConnectionID()), REQUEST);
			}
			
			// Save the route and map center for the Google Map
			ctx.setAttribute("googleMap", Boolean.TRUE, REQUEST);
			ctx.setAttribute("mapCenter", fr.getAirportD().getPosition().midPoint(fr.getAirportA().getPosition()), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set external status attribute
		ctx.setAttribute("extPIREP", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/pirepRead.jsp");
		result.setSuccess(true);
	}
}