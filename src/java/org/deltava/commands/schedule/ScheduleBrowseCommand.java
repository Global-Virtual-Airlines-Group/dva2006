// Copyright 2005, 2006, 2007, 2009, 2010, 2011, 2012, 2013, 2015, 2016, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Inclusion;
import org.deltava.beans.schedule.*;

import org.deltava.comparators.AirportComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to browse the Flight Schedule.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class ScheduleBrowseCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the departure airport
		Airport aD = SystemData.getAirport(ctx.getParameter("airportD"));
		if (aD == null)
			aD = SystemData.getAirport(ctx.getUser().getHomeAirport());

		// Build the search criteria
		ScheduleSearchCriteria criteria = new ScheduleSearchCriteria(null, 0, 0);
		criteria.setAirportD(aD);
		criteria.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
		criteria.setSortBy("AIRPORT_D, AIRPORT_A");
		criteria.setDBName(SystemData.get("airline.db"));
		criteria.setIncludeAcademy(ctx.isUserInRole("Instructor") || ctx.isUserInRole("Schedule") || ctx.isUserInRole("HR") || ctx.isUserInRole("AcademyAudit") || ctx.isUserInRole("AcademyAdmin") ? Inclusion.ALL : Inclusion.EXCLUDE);
		criteria.setExcludeHistoric(Inclusion.ALL);

		// Save the search criteria
		ctx.setAttribute("airportD", aD, REQUEST);
		ctx.setAttribute("airportA", criteria.getAirportA(), REQUEST);

		ViewContext<ScheduleEntry> vc = initView(ctx, ScheduleEntry.class);
		try {
			Connection con = ctx.getConnection();

			// Load schedule import metadata
			GetRawSchedule rsdao = new GetRawSchedule(con);
			Collection<ScheduleSourceInfo> srcs = rsdao.getSources(true);
			ctx.setAttribute("scheduleSources", srcs, REQUEST);

			// Load airports
			GetScheduleAirport dao = new GetScheduleAirport(con);
			AirportComparator ac = new AirportComparator(AirportComparator.NAME);
			List<Airport> airportsD = dao.getOriginAirports(null);
			List<Airport> airportsA = dao.getConnectingAirports(aD, true, null);
			Collections.sort(airportsD, ac);
			Collections.sort(airportsA, ac);
			ctx.setAttribute("airportsD", airportsD, REQUEST);
			ctx.setAttribute("airportsA", airportsA, REQUEST);

			// Search the schedule
			GetScheduleSearch sdao = new GetScheduleSearch(con);
			sdao.setQueryStart(vc.getStart());
			sdao.setQueryMax(vc.getCount());
			sdao.setSources(srcs);
			vc.setResults(sdao.search(criteria));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/browse.jsp");
		result.setSuccess(true);
	}
}