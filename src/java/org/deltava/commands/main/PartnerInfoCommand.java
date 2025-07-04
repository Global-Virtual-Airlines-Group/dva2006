// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import java.sql.Connection;
import java.util.Collection;

import org.deltava.beans.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PartnerAccessControl;

import org.deltava.util.BeanUtils;
import org.deltava.util.StringUtils;

/**
 * A Web Site Command to edit virtual airline Partner Information.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class PartnerInfoCommand extends AbstractAuditFormCommand {

	/**
	 * Callback method called when saving the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		boolean isNew = (ctx.getID() == 0);
		try {
			Connection con = ctx.getConnection();
			
			// Get the partner data
			PartnerInfo p = null, op = null;
			GetPartner dao = new GetPartner(con);
			if (!isNew) {
				p = dao.get(ctx.getID());
				if (p == null)
					throw notFoundException("Invalid Partner - " + ctx.getID());

				op = BeanUtils.clone(p);
				p.setName(ctx.getParameter("name"));
			} else
				p = new PartnerInfo(ctx.getParameter("name"));
			
			// Check our access
			PartnerAccessControl ac = new PartnerAccessControl(ctx, p);
			ac.validate();
			boolean doEdit = isNew? ac.getCanCreate() : ac.getCanEdit();
			if (!doEdit)
				throw securityException(String.format("Cannot %s Partner Information", isNew? "create" : "edit"));
			
			// Update the fields
			p.setURL(ctx.getParameter("url"));
			p.setDescription(ctx.getParameter("desc"));
			p.setPriority(StringUtils.parse(ctx.getParameter("priority"), 0));
			
			// Update the image
			FileUpload fu = ctx.getFile("img", 262144);
			if (fu != null)
				p.load(fu.getBuffer());
			else if (Boolean.parseBoolean(ctx.getParameter("deleteImg")))
				p.clear();
			
			// Check audit log
			Collection<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(op, p);
			AuditLog ae = AuditLog.create(p, delta, ctx.getUser().getID());
			
			// Start transaction
			ctx.startTX();
			
			// Save the partner
			SetPartner pwdao = new SetPartner(con);
			pwdao.write(p);
			
			// Write the audit log and commit
			writeAuditLog(ctx, ae);
			ctx.commitTX();
			
			// Save in request
			ctx.setAttribute("partner", p, REQUEST);
			ctx.setAttribute("isCreated", Boolean.valueOf(isNew), REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/main/partnerUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the profile.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		boolean isNew = (ctx.getID() == 0);
		try {
			GetPartner dao = new GetPartner(ctx.getConnection());
			PartnerInfo p = isNew ? null : dao.get(ctx.getID());
			if (!isNew && (p == null))
				throw notFoundException("Invalid Partner - " + ctx.getID());
			
			// Check our access
			PartnerAccessControl ac = new PartnerAccessControl(ctx, p);
			ac.validate();
			boolean doEdit = isNew? ac.getCanCreate() : ac.getCanEdit();
			if (!doEdit)
				throw securityException(String.format("Cannot %s Partner Information", isNew? "create" : "edit"));
			
			// Save in request
			readAuditLog(ctx, p);
			ctx.setAttribute("partner", p, REQUEST);
			ctx.setAttribute("access", ac, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/main/partnerEdit.jsp");
		result.setSuccess(true);
	}
	
	/**
	 * Callback method called when reading the profile. <i>Opens in edit mode</i>.
	 * @param ctx the Command context
	 * @see PartnerInfoCommand#execEdit(CommandContext)
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}