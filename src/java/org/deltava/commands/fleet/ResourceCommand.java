// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.fleet;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.fleet.Resource;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ResourceAccessControl;

/**
 * A Web Site Command to display/edit a Web Resource.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ResourceCommand extends AbstractFormCommand {

	/**
	 * Method called when saving the form.
	 * @param ctx the Command Context
	 * @throws CommandException if an unhandled error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the resource
			GetResources dao = new GetResources(con);
			Resource r = null;
			if (ctx.getID() == 0) {
				r = new Resource(ctx.getParameter("url"));
				r.setAuthorID(ctx.getUser().getID());
				r.setCreatedOn(new Date());
			} else {
				r = dao.get(ctx.getID());
				if (r == null)
					throw notFoundException("Invalid Web Resource ID - " + ctx.getID());
			}
			
			// Check our access
			ResourceAccessControl ac = new ResourceAccessControl(ctx, r);
			ac.validate();
			boolean canSave = (r.getID() == 0) ? ac.getCanCreate() : ac.getCanEdit();
			if (!canSave)
				throw securityException("Cannot edit Web Resource");

			// Check if we are doing a delete
			SetResource wdao = new SetResource(con);
			boolean doDelete = Boolean.valueOf(ctx.getParameter("doDelete")).booleanValue();
			if (doDelete) {
				wdao.delete(r.getID());
			} else {
				r.setURL(ctx.getParameter("url"));
				r.setDescription(ctx.getParameter("desc"));
				r.setLastUpdateID(ctx.getUser().getID());
				r.setPublic(ac.getCanEdit() && Boolean.valueOf(ctx.getParameter("isPublic")).booleanValue());
				wdao.write(r);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forword to the Command
		CommandResult result = ctx.getResult();
		result.setURL("resources.do");
		result.setType(CommandResult.REDIRECT);
		result.setSuccess(true);
	}

	/**
	 * Method called when editing the form.
	 * @param ctx the Command Context
	 * @throws CommandException if an unhandled error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {
		if (ctx.getID() != 0) {
			try {
				Connection con = ctx.getConnection();

				// Get the DAO and the resource
				GetResources dao = new GetResources(con);
				Resource r = dao.get(ctx.getID());
				if (r == null)
					throw notFoundException("Invalid Web Resource ID - " + ctx.getID());

				// Check our access
				ResourceAccessControl ac = new ResourceAccessControl(ctx, r);
				ac.validate();
				if (!ac.getCanEdit())
					throw securityException("Cannot edit Web Resource");

				// Load the author IDs
				Collection<Integer> IDs = new HashSet<Integer>();
				IDs.add(new Integer(r.getAuthorID()));
				IDs.add(new Integer(r.getLastUpdateID()));

				// Save the User names
				GetPilot pdao = new GetPilot(con);
				ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);

				// Save the resource in the request
				ctx.setAttribute("resource", r, REQUEST);
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}
		}
		
		// Check our Access
		ResourceAccessControl ac = new ResourceAccessControl(ctx, null);
		ac.validate();
		if (!ac.getCanCreate())
			throw securityException("Cannot create Web Resource");
		
		// Save access
		ctx.setAttribute("access", ac, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/fleet/resourceEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Method called when reading the form. <i>NOT IMPLEMENTED</i>
	 * @param ctx the Command Context
	 * @throws UnsupportedOperationException always
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		throw new UnsupportedOperationException();
	}
}