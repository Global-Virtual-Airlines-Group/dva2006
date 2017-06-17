// Copyright 2010, 2014, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.sql.Connection;
import java.util.Collection;

import org.deltava.beans.AuditLog;
import org.deltava.beans.academy.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.AcademyRideScriptAccessControl;

import org.deltava.util.*;

/**
 * A Web Site Command to handle Flight Academy Check Ride scripts.
 * @author Luke
 * @version 7.4
 * @since 3.4
 */

public class RideScriptCommand extends AbstractAuditFormCommand {

	/**
	 * Method called when saving the form.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		String id = (String) ctx.getCmdParameter(ID, null);
		boolean isNew = StringUtils.isEmpty(id);
		try {
			Connection con = ctx.getConnection();

			// Get the cert
			String certID = isNew ? ctx.getParameter("cert") : id.substring(0, id.lastIndexOf('-'));
			GetAcademyCertifications acdao = new GetAcademyCertifications(con);
			Certification c = acdao.get(certID);
			if (c == null)
				throw notFoundException("Unknown Certification - " + certID);
			
			// Get the script
			AcademyRideID rideID = isNew ? new AcademyRideID(c.getName() + "-" + ctx.getParameter("seq")) : new AcademyRideID(id);
			AcademyRideScript sc = acdao.getScript(rideID); AcademyRideScript oldSC = BeanUtils.clone(sc);
			if (!isNew && (sc == null))
				throw notFoundException("Academy Check Ride script not found - " + rideID);

			// Check our access
			AcademyRideScriptAccessControl ac = new AcademyRideScriptAccessControl(ctx, sc);
			ac.validate();
			if (isNew ? !ac.getCanCreate() : !ac.getCanEdit())
				throw securityException("Cannot create/edit Academy Check Ride script");
			
			// Build the bean
			if (sc == null)
				sc = new AcademyRideScript(c.getName(), StringUtils.parse(ctx.getParameter("seq"), 1));
				
			sc.setDescription(ctx.getParameter("body"));
			
			// Check audit log
			Collection<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(oldSC, sc);
			AuditLog ae = AuditLog.create(sc, delta, ctx.getUser().getID());
			
			// Start transaction
			ctx.startTX();
			
			// Write audit log
			writeAuditLog(ctx, ae);
			
			// Save the script
			SetAcademyCertification acwdao = new SetAcademyCertification(con);
			acwdao.write(sc);
			
			// Commit
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
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
			Connection con = ctx.getConnection();
			
			boolean isNew = StringUtils.isEmpty(id);
			AcademyRideScript sc = null;
			GetAcademyCertifications acdao = new GetAcademyCertifications(con);
			if (!isNew) {
				sc = acdao.getScript(new AcademyRideID(id));
				if (sc == null)
					throw notFoundException("Academy Check Ride script not found - " + id);
				
				readAuditLog(ctx, sc);
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