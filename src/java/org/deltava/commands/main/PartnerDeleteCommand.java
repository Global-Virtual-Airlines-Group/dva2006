// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import java.sql.Connection;

import org.deltava.beans.PartnerInfo;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PartnerAccessControl;

/**
 * A Web Site Command to delete virtual airline Partner information from the database.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class PartnerDeleteCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the partner
			GetPartner pdao = new GetPartner(con);
			PartnerInfo p = pdao.get(ctx.getID());
			if (p == null)
				throw notFoundException("Invalid Partner - " + ctx.getID());
			
			// Check our access
			PartnerAccessControl ac = new PartnerAccessControl(ctx, p);
			ac.validate();
			if (!ac.getCanDelete())
				throw securityException("Cannot delete Partner information");
			
			// Delete the data
			SetPartner pwdao = new SetPartner(con);
			pwdao.delete(p.getID());
			
			// Save in request
			ctx.setAttribute("partner", p, REQUEST);
			ctx.setAttribute("isDeleted", Boolean.TRUE, REQUEST);
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
}