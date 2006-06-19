// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.testing.CheckRide;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.CollectionUtils;

/**
 * A Web Site Command to display submitted Flight Academy Check Rides.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class RideQueueCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the view start/end
		ViewContext vc = initView(ctx);

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the CheckRides
			GetExam exdao = new GetExam(con);
			exdao.setQueryStart(vc.getStart());
			exdao.setQueryMax(vc.getCount());
			Collection<CheckRide> rides = exdao.getCheckRideQueue(true);
			vc.setResults(rides);
			
			// Build the CheckRide/Pilot IDs
			Collection<Integer> ids = new HashSet<Integer>();
			Collection<Integer> pids = new HashSet<Integer>();
			for (Iterator<CheckRide> i = rides.iterator(); i.hasNext(); ) {
				CheckRide cr = i.next();
				ids.add(new Integer(cr.getID()));
				pids.add(new Integer(cr.getPilotID()));
			}
			
			// Load the Courses
			GetAcademyCourses cdao = new GetAcademyCourses(con);
			ctx.setAttribute("courses", CollectionUtils.createMap(cdao.getByCheckRide(ids), "ID"), REQUEST);
			
			// Load the Pilots
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(pids, "PILOTS"), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/rideQueue.jsp");
		result.setSuccess(true);
	}
}