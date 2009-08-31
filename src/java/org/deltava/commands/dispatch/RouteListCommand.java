// Copyright 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.dispatch;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.acars.DispatchRoute;

import org.deltava.comparators.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.DispatchRouteAccessControl;

/**
 * A Web Site Command to display saved dispatch routes.
 * @author Luke
 * @version 2.6
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
		int authorID = ctx.getID();
		try {
			Connection con = ctx.getConnection();
			
			// Load the Author IDs and check if valid
			GetACARSRoute rdao = new GetACARSRoute(con);
			Collection<Integer> IDs = rdao.getAuthorIDs();
			if (!IDs.contains(new Integer(authorID)))
				authorID = 0;
			
			// Get the routes
			rdao.setQueryStart(vc.getStart());
			rdao.setQueryMax(vc.getCount());
			
			// Get the plans and filter
			Collection<DispatchRoute> plans = (authorID == 0) ? rdao.getAll(false, true) : rdao.getByAuthor(authorID);
			for (Iterator<DispatchRoute> i = plans.iterator(); i.hasNext(); ) {
				DispatchRoute rt = i.next();
				DispatchRouteAccessControl ac = new DispatchRouteAccessControl(ctx, rt);
				ac.validate();
				if (!ac.getCanView())
					i.remove();
			}
			
			vc.setResults(plans);
			
			// Get the user data
			GetPilot pdao = new GetPilot(con);
			GetUserData uddao = new GetUserData(con);
			UserDataMap udm = uddao.get(IDs);
			Map<Integer, Pilot> authors = pdao.get(udm);
			
			// Sort authors by name
			Collection<Pilot> pilots = new TreeSet<Pilot>(new PilotComparator(PersonComparator.LASTNAME));
			pilots.addAll(authors.values());
			
			// Save in the request
			ctx.setAttribute("authors", authors, REQUEST);
			ctx.setAttribute("author", authors.get(new Integer(authorID)), REQUEST);
			ctx.setAttribute("authorNames", pilots, REQUEST);
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