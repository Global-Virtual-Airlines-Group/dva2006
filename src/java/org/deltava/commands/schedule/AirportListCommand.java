// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.schedule.Airline;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display Airports.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class AirportListCommand extends AbstractViewCommand {
	
	private static final String[] SORT_TYPES = {"IATA", "ICAO", "NAME"};
	private static final List<ComboAlias> SORT_OPTIONS = ComboUtils.fromArray(new String[] {"IATA Code", "ICAO Code", "Airport Name"}, SORT_TYPES);

	/**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the start/end/count
		ViewContext vc = initView(ctx);
		String aCode = ctx.getParameter("airline");
		Airline a = SystemData.getAirline(aCode);
		if ((a == null) && !StringUtils.isEmpty(aCode)) {
		   a = SystemData.getAirline(SystemData.get("airline.code"));
		   
		   // THIS IS A HACK FOR AFV SINCE AIRLINE.CODE DOESN'T MATCH THE SCHEDULE DB
		   if (a == null)
		      a = SystemData.getAirline("AF");
		}
		
		// Get the sort type
		if (StringUtils.arrayIndexOf(SORT_TYPES, vc.getSortType()) == -1)
			vc.setSortType(SORT_TYPES[0]);
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the airports
			GetAirport dao = new GetAirport(con);
			dao.setQueryMax(vc.getCount());
			dao.setQueryStart(vc.getStart());
			
			// Save the results
			vc.setResults(dao.getByAirline(a, vc.getSortType()));
			
			// Get all airlines
			Collection<ComboAlias> airlines = new ArrayList<ComboAlias>();
			airlines.add(ComboUtils.fromString("All Airports", ""));
			airlines.addAll(SystemData.getAirlines().values());
			ctx.setAttribute("airlines", airlines, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save the airline / sort options
		ctx.setAttribute("airline", a, REQUEST);
		ctx.setAttribute("sortOptions", SORT_OPTIONS, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/airportList.jsp");
		result.setSuccess(true);
	}
}