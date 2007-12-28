// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.dispatch;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.acars.RoutePlan;

import org.deltava.comparators.*;
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
			Collection<RoutePlan> plans = (authorID == 0) ? rdao.getAll() : rdao.getByAuthor(authorID);
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