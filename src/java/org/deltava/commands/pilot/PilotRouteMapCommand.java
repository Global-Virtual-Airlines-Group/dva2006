// Copyright 2007, 2009, 2012, 2014, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.deltava.beans.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.ComboUtils;
import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to disply all the routes the pilot has flown.
 * @author Luke
 * @version 8.7
 * @since 1.0
 */

public class PilotRouteMapCommand extends AbstractCommand {
	
	private static final Collection<ComboAlias> DATE_OPTS = ComboUtils.fromArray(new String[] {"All Flights", "30 Days", "90 Days", "180 Days", "1 Year", "2 Years", "5 Years", "10 Years", "15 Years", "20 Years"}, 
			new String[] {"0", "30", "90", "180", "365", "720", "1825", "3650", "5475", "7300"});

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the pilot ID
		int userID = ctx.getUser().getID();
		if ((ctx.isUserInRole("PIREP") || ctx.isUserInRole("HR")) && (ctx.getID() != 0))
			userID = ctx.getID();
		
		try {
			Connection con = ctx.getConnection();
			GetPilot pdao = new GetPilot(con);
			Pilot usr = pdao.get(userID);
			if (usr == null)
				throw notFoundException("Unknown Pilot ID - " + userID);
			
			// Get the first flight date
			GetFlightReports frdao = new GetFlightReports(con);
			FlightReport fr = frdao.getByPilot(userID, new ScheduleSearchCriteria("DATE, PR.SUBMITTED")).stream().filter(f -> (f.getStatus() != FlightStatus.DRAFT && f.getStatus() != FlightStatus.REJECTED)).findFirst().orElse(null);
			Instant firstFlight = (fr != null) ? fr.getDate() : Instant.now().minus(1, ChronoUnit.DAYS);
			long days = Duration.between(firstFlight, Instant.now()).toDays();
			
			// Filter the dateOps
			Collection<ComboAlias> dateOpts = DATE_OPTS.stream().filter(ca -> (StringUtils.parse(ca.getComboAlias(), Integer.MAX_VALUE) < days)).collect(Collectors.toList());
			
			// Save the user's home airport
			Airport airportH = SystemData.getAirport(usr.getHomeAirport());
			if (airportH == null)
				airportH = SystemData.getAirport("LFPG");
			
			// Save in request
			ctx.setAttribute("dateOptions", dateOpts, REQUEST);
			ctx.setAttribute("home", airportH, REQUEST);
			ctx.setAttribute("pilot", usr, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/myRouteMap.jsp");
		result.setSuccess(true);
	}
}