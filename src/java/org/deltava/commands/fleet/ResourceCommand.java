// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.fleet;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.fleet.Resource;

import org.deltava.commands.*;
import org.deltava.dao.*;


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
		
		// Check our access
		boolean isNew = (ctx.getID() == 0);
		if ((!isNew) && (ctx.getRoles().size() <= 1))
			throw securityException("Cannot edit Web Resource");

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the resource
			GetResources dao = new GetResources(con);
			Resource r = null;
			if (isNew) {
				r = new Resource(ctx.getParameter("url"));
				r.setAuthorID(ctx.getUser().getID());
				r.setCreatedOn(new Date());
			} else {
				r = dao.get(ctx.getID());
				if (r == null)
					throw notFoundException("Invalid Web Resource ID - " + ctx.getID());
			}
			
			// Check if we are doing a delete
			SetResource wdao = new SetResource(con);
			boolean doDelete = Boolean.valueOf(ctx.getParameter("doDelete")).booleanValue();
			if (doDelete) {
				if (!ctx.isUserInRole("HR"))
					throw securityException("Cannot delete Web Resource");
				
				wdao.delete(r.getID());
			} else {
				r.setURL(ctx.getParameter("url"));
				r.setDescription(ctx.getParameter("desc"));
				r.setLastUpdateID(ctx.getUser().getID());
				r.setPublic(Boolean.valueOf(ctx.getParameter("isPublic")).booleanValue());
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
		
		// Check if we can edit
		boolean isNew = (ctx.getID() == 0);
		if ((!isNew) && (ctx.getRoles().size() <= 1))
			throw securityException("Cannot edit Web Resource");
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the resource
			if (!isNew) {
				GetResources dao = new GetResources(con);
				Resource r = dao.get(ctx.getID());
				if (r == null)
					throw notFoundException("Invalid Web Resource ID - " + ctx.getID());
				
				// Load the author IDs
				Collection<Integer> IDs = new HashSet<Integer>();
				IDs.add(new Integer(r.getAuthorID()));
				IDs.add(new Integer(r.getLastUpdateID()));
				
				// Save the User names
				GetPilot pdao = new GetPilot(con);
				ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
				
				// Save the resource in the request
				ctx.setAttribute("resource", r, REQUEST);
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

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