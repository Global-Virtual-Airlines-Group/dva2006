// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.sql.Connection;

import org.deltava.beans.academy.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.AcademyRideScriptAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to handle Flight Academy Check Ride scripts.
 * @author Luke
 * @version 3.4
 * @since 3.4
 */

public class RideScriptCommand extends AbstractFormCommand {

	/**
	 * Method called when saving the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		String id = (String) ctx.getCmdParameter(ID, null);		
		try {
			boolean isNew = StringUtils.isEmpty(id);
			Connection con = ctx.getConnection();

			// Get the script
			GetAcademyCertifications acdao = new GetAcademyCertifications(con);
			AcademyRideScript sc = acdao.getScript(id);
			if (!isNew && (sc == null))
				throw notFoundException("Academy Check Ride script not found - " + id);

			// Check our access
			AcademyRideScriptAccessControl ac = new AcademyRideScriptAccessControl(ctx, sc);
			ac.validate();
			if (isNew ? !ac.getCanCreate() : !ac.getCanEdit())
				throw securityException("Cannot create/edit Academy Check Ride script");
			
			// Build the bean
			if (sc == null) {
				String certID = ctx.getParameter("cert");
				Certification c = acdao.get(certID);
				if (c == null)
					throw notFoundException("Unknown Certification - " + certID);
				
				sc = new AcademyRideScript(c.getName());
			}
				
			sc.setDescription(ctx.getParameter("body"));
			
			// Save the script
			SetAcademyCertification acwdao = new SetAcademyCertification(con);
			acwdao.write(sc);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the script list
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("arscripts.do");
		result.setSuccess(true);
	}

	/**
	 * Method called when editing the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		String id = (String) ctx.getCmdParameter(ID, null);
		try {
			boolean isNew = StringUtils.isEmpty(id);
			AcademyRideScript sc = null;
			GetAcademyCertifications acdao = new GetAcademyCertifications(ctx.getConnection());
			if (!isNew) {
				sc = acdao.getScript(id);
				if (sc == null)
					throw notFoundException("Academy Check Ride script not found - " + id);
			}
			
			// Check our access
			AcademyRideScriptAccessControl ac = new AcademyRideScriptAccessControl(ctx, sc);
			ac.validate();
			if (isNew ? !ac.getCanCreate() : !ac.getCanEdit())
				throw securityException("Cannot create/edit Academy Check Ride script");
			
			// Load certifications
			if (isNew)
				ctx.setAttribute("certs", acdao.getAll(), REQUEST);
			
			// Set request attributes
			ctx.setAttribute("sc", sc, REQUEST);
			ctx.setAttribute("access", ac, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/crScript.jsp");
		result.setSuccess(true);
	}

	/**
	 * Method called when reading the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}