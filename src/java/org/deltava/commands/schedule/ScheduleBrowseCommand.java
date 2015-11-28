// Copyright 2005, 2006, 2007, 2009, 2010, 2011, 2012, 2013, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.sql.Connection;

import org.deltava.beans.schedule.*;
import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to browse the Flight Schedule.
 * @author Luke
 * @version 6.3
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

		// Get the view context
		ViewContext vc = initView(ctx);

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
		criteria.setIncludeAcademy(ctx.isUserInRole("Instructor") || ctx.isUserInRole("Schedule") || ctx.isUserInRole("HR")
				|| ctx.isUserInRole("AcademyAudit"));

		// Save the search criteria
		ctx.setAttribute("airportD", aD, REQUEST);
		ctx.setAttribute("airportA", criteria.getAirportA(), REQUEST);
		if (ctx.isAuthenticated())
			ctx.setAttribute("useICAO", Boolean.valueOf(ctx.getUser().getAirportCodeType() == Airport.Code.ICAO), REQUEST);

		// Do the search
		try {
			Connection con = ctx.getConnection();
			
			// Load airports
			GetScheduleAirport dao = new GetScheduleAirport(con);
			ctx.setAttribute("airportsD",dao.getOriginAirports(null), REQUEST);
			ctx.setAttribute("airportsA", dao.getConnectingAirports(aD, true, null), REQUEST);

			// Search the schedule
			GetScheduleSearch sdao = new GetScheduleSearch(con);
			sdao.setQueryStart(vc.getStart());
			sdao.setQueryMax(vc.getCount());
			vc.setResults(sdao.search(criteria));

			// Load schedule import data
			GetMetadata mddao = new GetMetadata(con);
			String aCode = SystemData.get("airline.code").toLowerCase();
			ctx.setAttribute("importDate", mddao.getDate(aCode + ".schedule.import"), REQUEST);
			ctx.setAttribute("effectiveDate", mddao.getDate(aCode + ".schedule.effDate"), REQUEST);
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