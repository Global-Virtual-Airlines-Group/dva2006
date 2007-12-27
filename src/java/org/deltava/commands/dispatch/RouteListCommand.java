// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.dispatch;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.acars.RoutePlan;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display saved dispatch routes.
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public class RouteListCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the view context
		ViewContext vc = initView(ctx);
		try {
			Connection con = ctx.getConnection();
			
			// Get the routes
			GetACARSRoute rdao = new GetACARSRoute(con);
			rdao.setQueryStart(vc.getStart());
			rdao.setQueryMax(vc.getCount());
			Collection<RoutePlan> plans = rdao.getAll();
			
			// Load the Author IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Iterator<RoutePlan> i = plans.iterator(); i.hasNext(); ) {
				RoutePlan rp = i.next();
				IDs.add(new Integer(rp.getAuthorID()));
			}

			// Save the results
			vc.setResults(plans);
			
			// Get the user data
			GetPilot pdao = new GetPilot(con);
			GetUserData uddao = new GetUserData(con);
			UserDataMap udm = uddao.get(IDs);
			ctx.setAttribute("authors", pdao.get(udm), REQUEST);
			ctx.setAttribute("userData", udm, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/dispatch/routeList.jsp");
		result.setSuccess(true);
	}
}