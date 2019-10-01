// Copyright 2008, 2009, 2010, 2011, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.cooler.SignatureImage;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to approve a Pilot's Water Cooler signature image.
 * @author Luke
 * @version 8.7
 * @since 2.3
 */

public class SignatureApproveCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the signature
			GetImage idao = new GetImage(con);
			boolean isAuth = idao.isSignatureAuthorized(ctx.getID());
			
			// Load the Pilot profile
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(ctx.getID());
			ctx.setAttribute("pilot", p, REQUEST);
			
			// If not authorized, give it the "seal of approval"
			if (!isAuth) {
				SignatureImage si = new SignatureImage(p.getID());
				si.load(idao.getSignatureImage(ctx.getID(), SystemData.get("airline.db")));
				si.watermark("Approved Signature", si.getWidth() - 120, si.getHeight() - 4);
				p.load(si.getImage("png"));
				
				// Create status update
				StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.COMMENT);
				upd.setAuthorID(ctx.getUser().getID());
				upd.setDescription("Approved Signature");
				
				// Start transaction
				ctx.startTX();
				
				// Save the data
				SetStatusUpdate sudao = new SetStatusUpdate(con);
				SetSignatureImage swdao = new SetSignatureImage(con);
				swdao.write(p, si.getWidth(), si.getHeight(), "png", true);
				sudao.write(upd);
				ctx.commitTX();
			}
		} catch (Exception e) {
			ctx.rollbackTX();
			throw new CommandException(e);
		} finally {
			ctx.release();
		}
		
        // Set status variable for the result JSP
        ctx.setAttribute("sigUpdated", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
        result.setType(ResultType.REQREDIRECT);
        result.setURL("/jsp/pilot/pilotUpdate.jsp");
		result.setSuccess(true);
	}
}