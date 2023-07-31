// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.deltava.beans.testing.CheckRide;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display pending Check Rides.
 * @author Luke
 * @version 11.1
 * @since 11.1
 */

public class CheckRideQueueCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the context
		ViewContext<CheckRide> vc = initView(ctx, CheckRide.class);
		try {
			Connection con = ctx.getConnection();
			
			// Get the check rides
			GetExam exdao = new GetExam(con);
			exdao.setQueryStart(vc.getStart());
			exdao.setQueryMax(vc.getCount());
			vc.setResults(exdao.getSubmittedRides());
			
			// Load the Pilots
			Collection<Integer> IDs = vc.getResults().stream().map(CheckRide::getAuthorID).collect(Collectors.toSet());
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/testing/cRideQueue.jsp");
		result.setSuccess(true);
	}
}