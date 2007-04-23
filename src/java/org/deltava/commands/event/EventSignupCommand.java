// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.event;

import java.sql.Connection;

import org.deltava.beans.event.*;
import org.deltava.commands.*;

import org.deltava.dao.GetEvent;
import org.deltava.dao.SetEvent;
import org.deltava.dao.DAOException;

import org.deltava.security.command.EventAccessControl;
import org.deltava.security.command.SignupAccessControl;

/**
 * A Web Site Command to sign up Pilots for an Online Event.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EventSignupCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check if we are canceling our signup
		boolean isCancel = "cancel".equals(ctx.getCmdParameter(Command.OPERATION, null));
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the online event
			GetEvent dao = new GetEvent(con);
			Event e = dao.get(ctx.getID());
			if (e == null)
				throw notFoundException("Invalid Online Event - " + ctx.getID());
			
			// Check our access to sign up or cancel
			boolean ourAccess = false;
			if (isCancel) {
				Signup s = e.getSignup(ctx.getUser().getID());
				if (s == null)
					throw notFoundException("Not Signed up for " + e.getName());
				
				SignupAccessControl access = new SignupAccessControl(ctx, e, s);
				access.validate();
				ourAccess = access.getCanRelease();
			} else {
				EventAccessControl access = new EventAccessControl(ctx, e);
				access.validate();
				ourAccess = access.getCanSignup();
			}

			// If we're not authorized, halt
			if (!ourAccess)
				throw securityException("Cannot sign up for Online Event " + e.getName());
			
			// Find the route
			Route r = e.getRoute(ctx.getParameter("route"));
			if (r == null)
				throw notFoundException("Invalid Event Route - " + ctx.getParameter("route"));
			
			// Make sure it's active, or we're in the Event role
			if (!r.getActive() && !ctx.isUserInRole("Event"))
				throw securityException("Inactive Route - " + r);
			
			// Create the signup from the request
			Signup s = new Signup(e.getID(), ctx.getUser().getID());
			s.setEquipmentType(ctx.getParameter("eqType"));
			s.setAirportA(r.getAirportA());
			s.setAirportD(r.getAirportD());
			
			// Get the DAO and sign up
			SetEvent wdao = new SetEvent(con);
			wdao.signup(s);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP - show the event
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REDIRECT);
		result.setURL("event", null, ctx.getID());
		result.setSuccess(true);
	}
}