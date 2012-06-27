// Copyright 2005, 2007, 2008, 2009, 2010, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.flight.FlightReport;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.ComboUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to plot a flight route.
 * @author Luke
 * @version 4.2
 * @since 1.0
 */

public class RoutePlotCommand extends AbstractCommand {
	
	private static final List<?> SIM_VERSIONS = ComboUtils.fromArray(new String[] {"Flight Simulator X", 
			"Flight Simulator 2004", "X-Plane 9", "Prepar3D"}, new String[] {"FSX", "FS9", "XP9", "P3D"}); 

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Look for a draft PIREP
			GetFlightReports frdao = new GetFlightReports(con);
			FlightReport dfr = frdao.get(ctx.getID());
			if (dfr != null) {
				ctx.setAttribute("flight", dfr, REQUEST);
				ctx.setAttribute("airlines", Collections.singleton(dfr.getAirline()), REQUEST);
				ctx.setAttribute("airportsD", Collections.singleton(dfr.getAirportD()), REQUEST);
				ctx.setAttribute("airportsA", Collections.singleton(dfr.getAirportA()), REQUEST);
			} else {
				ctx.setAttribute("airlines", SystemData.getAirlines().values(), REQUEST);
				ctx.setAttribute("airportsD", Collections.emptyList(), REQUEST);
				ctx.setAttribute("airportsA", Collections.emptyList(), REQUEST);
			}
			
			// Load aircraft types
			GetAircraft acdao = new GetAircraft(con);
			ctx.setAttribute("eqTypes", acdao.getAircraftTypes(SystemData.get("airline.code")), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set request attributes
		ctx.setAttribute("simVersions", SIM_VERSIONS, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/routePlot.jsp");
		result.setSuccess(true);
	}
}