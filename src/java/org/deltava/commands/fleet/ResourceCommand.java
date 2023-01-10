// Copyright 2006, 2009, 2016, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.fleet;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.AuditLog;
import org.deltava.beans.fleet.Resource;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ResourceAccessControl;

import org.deltava.util.BeanUtils;

/**
 * A Web Site Command to display/edit a Web Resource.
 * @author Luke
 * @version 10.4
 * @since 1.0
 */

public class ResourceCommand extends AbstractAuditFormCommand {

	/**
	 * Method called when saving the form.
	 * @param ctx the Command Context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the resource
			GetResources dao = new GetResources(con);
			Resource r = null, or = null;
			if (ctx.getID() == 0) {
				r = new Resource(ctx.getParameter("url"));
				r.setAuthorID(ctx.getUser().getID());
				r.setCreatedOn(Instant.now());
			} else {
				r = dao.get(ctx.getID());
				if (r == null)
					throw notFoundException("Invalid Web Resource ID - " + ctx.getID());
				
				or = BeanUtils.clone(r);
			}
			
			// Check our access
			ResourceAccessControl ac = new ResourceAccessControl(ctx, r);
			ac.validate();
			boolean canSave = (r.getID() == 0) ? ac.getCanCreate() : ac.getCanEdit();
			if (!canSave)
				throw securityException(String.format("Cannot %s Web Resource", (r.getID() == 0) ? "create" : "edit"));

			// Copy fields from request
			r.setTitle(ctx.getParameter("title"));
			r.setURL(ctx.getParameter("url"));
			r.setCategory(ctx.getParameter("category"));
			r.setDescription(ctx.getParameter("desc"));
			r.setLastUpdateID(ctx.getUser().getID());
			r.setPublic(ac.getCanEdit() && Boolean.parseBoolean(ctx.getParameter("isPublic")));
			
			// Populate Flight Academy Certifications
			boolean hasCerts = Boolean.parseBoolean(ctx.getParameter("hasCerts"));
			if (hasCerts) {
				r.addCertifications(ctx.getParameters("certNames", Collections.emptySet()));
				r.setPublic(true);
			}
			
			r.setIgnoreCertifcations(Boolean.parseBoolean(ctx.getParameter("ignoreCerts")) && !r.getCertifications().isEmpty());
			
			// Check audit log
			Collection<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(or, r);
			AuditLog ae = AuditLog.create(r, delta, ctx.getUser().getID());
			
			// Start transaction
			ctx.startTX();
			
			// Save the resource
			SetResource wdao = new SetResource(con);
			wdao.write(r);
			writeAuditLog(ctx, ae);
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the Command
		CommandResult result = ctx.getResult();
		result.setURL("resources.do");
		result.setType(ResultType.REDIRECT);
		result.setSuccess(true);
	}

	/**
	 * Method called when editing the form.
	 * @param ctx the Command Context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the resource
			if (ctx.getID() != 0) {
				GetResources dao = new GetResources(con);
				Resource r = dao.get(ctx.getID());
				if (r == null)
					throw notFoundException("Invalid Web Resource ID - " + ctx.getID());

				// Check our access
				ResourceAccessControl ac = new ResourceAccessControl(ctx, r);
				ac.validate();
				if (!ac.getCanEdit())
					throw securityException("Cannot edit Web Resource");

				// Load audit log
				readAuditLog(ctx, r);

				// Load the authors
				GetPilot pdao = new GetPilot(con);
				ctx.setAttribute("pilots", pdao.getByID(new HashSet<Integer>(List.of(Integer.valueOf(r.getAuthorID()), Integer.valueOf(r.getLastUpdateID()))), "PILOTS"), REQUEST);
				
				// Save in request
				ctx.setAttribute("resource", r, REQUEST);
				ctx.setAttribute("access", ac, REQUEST);
			} else {
				// Check our Access
				ResourceAccessControl ac = new ResourceAccessControl(ctx, null);
				ac.validate();
				if (!ac.getCanCreate())
					throw securityException("Cannot create Web Resource");
			}
				
			// Get Flight Academy certs
			GetAcademyCertifications cdao = new GetAcademyCertifications(con);
			ctx.setAttribute("certs", cdao.getAll(), REQUEST);
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
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}