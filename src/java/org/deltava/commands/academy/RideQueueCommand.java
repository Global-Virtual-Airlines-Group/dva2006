// Copyright 2006, 2010, 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.testing.CheckRide;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.CollectionUtils;

/**
 * A Web Site Command to display submitted Flight Academy Check Rides.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class RideQueueCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the view start/end
		ViewContext<CheckRide> vc = initView(ctx, CheckRide.class);
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the CheckRides
			GetExam exdao = new GetExam(con);
			exdao.setQueryStart(vc.getStart());
			exdao.setQueryMax(vc.getCount());
			vc.setResults(exdao.getCheckRideQueue(true));
			
			// Get the user data / flight report DAOs
			GetUserData uddao = new GetUserData(con);
			GetFlightReports frdao = new GetFlightReports(con);
			
			// Build the CheckRide/Pilot IDs and load Flight Reports
			Collection<Integer> ids = new HashSet<Integer>();
			Collection<Integer> pids = new HashSet<Integer>();
			Map<Integer, FlightReport> pireps = new HashMap<Integer, FlightReport>();
			for (CheckRide cr : vc.getResults()) {
				ids.add(Integer.valueOf(cr.getID()));
				pids.add(Integer.valueOf(cr.getAuthorID()));
				
				// Load the PIREP
				UserData ud = uddao.get(cr.getAuthorID());
				FlightReport fr = frdao.getACARS(ud.getDB(), cr.getFlightID());
				if (fr != null)
					pireps.put(Integer.valueOf(ud.getID()), fr);
			}
			
			// Load the Courses
			GetAcademyCourses cdao = new GetAcademyCourses(con);
			ctx.setAttribute("courses", CollectionUtils.createMap(cdao.getByCheckRide(ids), "ID"), REQUEST);
			
			// Load the Pilots and pireps
			GetPilot pdao = new GetPilot(con);
			UserDataMap udm = uddao.get(pids);
			ctx.setAttribute("userData", udm, REQUEST);
			ctx.setAttribute("pilots", pdao.get(udm), REQUEST);
			ctx.setAttribute("pireps", pireps, REQUEST);
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