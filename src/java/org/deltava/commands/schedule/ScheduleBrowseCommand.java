// Copyright 2005, 2006, 2007, 2009, 2010, 2011, 2012, 2013, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.schedule.*;
import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to browse the Flight Schedule.
 * @author Luke
 * @version 7.0
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

		try {
			Connection con = ctx.getConnection();
			
	         // Load schedule import metadata
	    	 GetMetadata mddao = new GetMetadata(con);
	    	 String aCode = SystemData.get("airline.code").toLowerCase();
	    	 ctx.setAttribute("importDate", mddao.getDate(aCode + ".schedule.import"), REQUEST);
	    	 Instant effDate = mddao.getDate(aCode + ".schedule.effDate");
	    	 ctx.setAttribute("effectiveDate", effDate, REQUEST);
			
			// Load airports
			GetScheduleAirport dao = new GetScheduleAirport(con);
			ctx.setAttribute("airportsD",dao.getOriginAirports(null), REQUEST);
			ctx.setAttribute("airportsA", dao.getConnectingAirports(aD, true, null), REQUEST);

			// Search the schedule
			GetScheduleSearch sdao = new GetScheduleSearch(con);
			sdao.setQueryStart(vc.getStart());
			sdao.setQueryMax(vc.getCount());
			sdao.setEffectiveDate(effDate);
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