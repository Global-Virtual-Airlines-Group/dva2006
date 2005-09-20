// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.schedule;

import java.sql.Connection;

import org.deltava.beans.schedule.Airline;

import org.deltava.commands.*;

import org.deltava.dao.GetAirport;
import org.deltava.dao.DAOException;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display Airports.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AirportListCommand extends AbstractViewCommand {

	/**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the start/end/count
		ViewContext vc = initView(ctx);
		String aCode = (String) ctx.getCmdParameter(ID, null);
		Airline a = SystemData.getAirline((aCode == null) ? SystemData.get("airline.code") : aCode);
		if (a == null)
		   a = SystemData.getAirline(SystemData.get("airline.code"));
		
		// Save the airline
		ctx.setAttribute("airline", a, REQUEST);

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the airports
			GetAirport dao = new GetAirport(con);
			dao.setQueryMax(vc.getCount());
			dao.setQueryStart(vc.getStart());
			
			// Save the results
			vc.setResults(dao.getByAirline(a));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/airportList.jsp");
		result.setSuccess(true);
	}
}