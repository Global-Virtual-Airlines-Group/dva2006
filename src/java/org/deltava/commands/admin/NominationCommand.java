// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.hr.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.NominationAccessControl;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle Senior Captain nominations.
 * @author Luke
 * @version 3.3
 * @since 3.3
 */

public class NominationCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving the nomination.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();

			// Load the nomination - If no nomination, create one
			GetNominations ndao = new GetNominations(con);
			Nomination n = ndao.get(ctx.getID());
			if (n == null) 
				n = new Nomination(ctx.getID());
			
			// Check we haven't already commented
			if (n.hasComment(ctx.getUser().getID()) && !ctx.isUserInRole("HR"))
				throw securityException("Already commented on Nomination " + n.getID());
			
			// Add comment
			NominationComment nc = new NominationComment(ctx.getUser().getID(), ctx.getParameter("body"));
			nc.setCreatedOn(new Date());
			
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
	}

	/**
	 * Callback method called when editing the nomination.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Load the Nomination
			GetNominations ndao = new GetNominations(con);
			Nomination n = ndao.get(ctx.getID());
			if (n == null)
				throw notFoundException("Unknown Nomination - " + ctx.getID());
			
			// Check our access
			NominationAccessControl access = new NominationAccessControl(ctx, n);
			access.validate();
			if (!access.getCanUpdate())
				throw securityException("Cannot update Nomination");
			
			// Save in request
			ctx.setAttribute("nom", n, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
			
			// Get pilot IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			IDs.add(new Integer(n.getID()));
			for (NominationComment nc : n.getComments())
				IDs.add(new Integer(nc.getAuthorID()));
			
			// Load Pilots
			GetPilot pdao = new GetPilot(con);
			GetFlightReports frdao = new GetFlightReports(con);
			Map<Integer, Pilot> pilots = pdao.getByID(IDs, "PILOTS");
			frdao.getOnlineTotals(pilots, SystemData.get("airline.db"));
			ctx.setAttribute("pilots", pilots, REQUEST);
			
			// Load nominee's status history
			GetStatusUpdate sudao = new GetStatusUpdate(con);
			ctx.setAttribute("updates", sudao.getByUser(n.getID(), SystemData.get("airline.db")), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/scNomEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the nomination.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}