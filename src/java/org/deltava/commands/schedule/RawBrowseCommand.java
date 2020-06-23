// Copyright 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.schedule.*;

import org.deltava.comparators.AirportComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.EnumUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to browse the raw Flight Schedule.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class RawBrowseCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the source and departure airport
		ScheduleSource src = EnumUtils.parse(ScheduleSource.class, ctx.getParameter("src"), ScheduleSource.MANUAL);
		Airport aD = SystemData.getAirport(ctx.getParameter("airportD"));
		Airport aA = SystemData.getAirport(ctx.getParameter("airportA"));

		ViewContext<RawScheduleEntry> vc = initView(ctx, RawScheduleEntry.class);
		try {
			Connection con = ctx.getConnection();
			
			// Load departure airports
			AirportComparator ac = new AirportComparator(AirportComparator.NAME);
			GetRawScheduleInfo ridao = new GetRawScheduleInfo(con);
			List<Airport> airportsD = ridao.getOriginAirports(src, null);
			Collections.sort(airportsD, ac);
			
			// Set departure airport if not specified
			if ((aD == null) && (aA == null) && !airportsD.isEmpty())
				aD = airportsD.get(0);
			
			// Load arrival airports
			List<Airport> airportsA = ridao.getArrivalAirports(src, aD);
			Collections.sort(airportsA, ac);
			ctx.setAttribute("airportsD", airportsD, REQUEST);
			ctx.setAttribute("airportsA", airportsA, REQUEST);
			
			// Search the schedule
			GetRawSchedule sdao = new GetRawSchedule(con);
			sdao.setQueryStart(vc.getStart());
			sdao.setQueryMax(vc.getCount());
			vc.setResults(sdao.list(src, aD, aA));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save the search criteria
		ctx.setAttribute("airportD", aD, REQUEST);
		ctx.setAttribute("airportA", aA, REQUEST);
		ctx.setAttribute("src", src, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/browseRaw.jsp");
		result.setSuccess(true);
	}
}