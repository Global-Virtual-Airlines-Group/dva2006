// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.event;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.event.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.EventAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to balance signups between routes.
 * @author Luke
 * @version 2.3
 * @since 2.3
 */

public class EventBalanceCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get comamnd result
		CommandResult result = ctx.getResult();
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the event
			GetEvent edao = new GetEvent(con);
			Event e =  edao.get(ctx.getID());
			if (e == null)
				throw notFoundException("Invalid Online Event - " + ctx.getID());
			
			// Check out access
			EventAccessControl ac = new EventAccessControl(ctx, e);
			ac.validate();
			if (!ac.getCanBalance())
				throw securityException("Cannot balance Signups");
			
			// Get Pilot IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Signup s : e.getSignups())
				IDs.add(new Integer(s.getPilotID()));
			
			// Load Pilots
			GetUserData uddao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			UserDataMap udm = uddao.get(IDs);
			
			// Save the event and pilots
			ctx.setAttribute("event", e, REQUEST);
			ctx.setAttribute("userLocs", udm, REQUEST);
			ctx.setAttribute("pilots", pdao.get(udm), REQUEST);
			
			// If we're not rebalancing, forward to the JSP
			if (ctx.getParameter("routeID") == null) {
				ctx.release();
				result.setURL("/jsp/event/signupBalance.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Get the route to assign to
			Route r = e.getRoute(StringUtils.parse(ctx.getParameter("routeID"), 0));
			if (r == null)
				throw notFoundException("Invalid Route ID - " + ctx.getParameter("routeID"));
			
			// Start transaction
			ctx.startTX();
			
			// Update the signups
			SetEvent ewdao = new SetEvent(con);
			for (Iterator<String> i = ctx.getParameters("signupID").iterator(); i.hasNext(); ) {
				int id = StringUtils.parseHex(i.next());
				Signup s = e.getSignup(id);
				if (s.getRouteID() != r.getRouteID()) {
					s.setRouteID(r.getRouteID());
					ewdao.signup(s);
				}
			}
			
			// Commit the transaction and save updated signups
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		result.setURL("/jsp/event/signupBalance.jsp");
		result.setSuccess(true);
	}
}