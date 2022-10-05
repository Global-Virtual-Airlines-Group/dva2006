// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import java.sql.Connection;

import org.deltava.beans.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PartnerAccessControl;

/**
 * A Web Site Command to edit virtual airline Partner Information.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class PartnerInfoCommand extends AbstractFormCommand {

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
			GetPartner dao = new GetPartner(con);
			PartnerInfo p = isNew ? new PartnerInfo(ctx.getParameter("name")) : dao.get(ctx.getID());
			if (!isNew && (p == null))
				throw notFoundException("Invalid Partner - " + ctx.getID());
			
			// Check our access
			PartnerAccessControl ac = new PartnerAccessControl(ctx, p);
			ac.validate();
			boolean doEdit = isNew? ac.getCanCreate() : ac.getCanEdit();
			if (!doEdit)
				throw securityException(String.format("Cannot %s Partner Information", isNew? "create" : "edit"));
			
			// Update the fields
			p.setName(ctx.getParameter("name"));
			p.setURL(ctx.getParameter("url"));
			p.setDescription(ctx.getParameter("desc"));
			
			// Update the image
			FileUpload fu = ctx.getFile("img", 262144);
			if (fu != null)
				p.load(fu.getBuffer());
			else if (Boolean.parseBoolean(ctx.getParameter("deleteImg")))
				p.clear();
			
			// Save the partner
			SetPartner pwdao = new SetPartner(con);
			pwdao.write(p);
			
			// Save in request
			ctx.setAttribute("partner", p, REQUEST);
			ctx.setAttribute("isCreated", Boolean.valueOf(isNew), REQUEST);
		} catch (DAOException de) {
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