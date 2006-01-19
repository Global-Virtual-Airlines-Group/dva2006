// Copyright 2005, 2006 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.commands.security;

import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.StatusUpdate;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.Authenticator;
import org.deltava.security.command.PilotAccessControl;

import org.deltava.util.PasswordGenerator;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to reactivate a Pilot.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */
public class PilotActivationCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command result
		CommandResult result = ctx.getResult();

		// Initialize the Message context
		MessageContext mctx = new MessageContext();
		mctx.addData("user", ctx.getUser());

		Pilot p = null;
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Pilot profile
			GetPilot dao = new GetPilot(con);
			p = dao.get(ctx.getID());
			if (p == null)
				throw new CommandException("Invalid Pilot - " + ctx.getID());

			// Check our access level
			PilotAccessControl access = new PilotAccessControl(ctx, p);
			access.validate();
			if (!access.getCanActivate())
				throw securityException("Cannot activate Pilot");

			// Get the equipment program
			GetEquipmentType eqdao = new GetEquipmentType(con);
			ctx.setAttribute("eqType", eqdao.get(p.getEquipmentType()), REQUEST);

			// Save the pilot in the request
			ctx.setAttribute("pilot", p, REQUEST);
			mctx.addData("pilot", p);

			// If we're doing a GET, redirect to the JSP
			if (ctx.getParameter("eMail") == null) {
				result.setURL("/jsp/admin/activatePilot.jsp");
			} else {
				// Update the pilot status
				p.setEmail(ctx.getParameter("eMail"));
				p.setRank(ctx.getParameter("rank"));
				p.setStatus(Pilot.ACTIVE);

				// Reset the password
				p.setPassword(PasswordGenerator.generate(8));

				// Get the Message Template DAO
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctx.setTemplate(mtdao.get("USERACTIVATE"));
				
				// Create the status update entry
				StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.STATUS_CHANGE);
				upd.setAuthorID(ctx.getUser().getID());
				upd.setDescription("Returned to Active status");

				// Start the transaction
				ctx.startTX();

				// Get the write DAO and save the pilot
				SetPilot pwdao = new SetPilot(con);
				pwdao.write(p);
				
				// Write the status update entry
				SetStatusUpdate sudao = new SetStatusUpdate(con);
				sudao.write(upd);

				// Get the authenticator and update the password
				Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
				if (auth.contains(p)) {
					auth.updatePassword(p, p.getPassword());
				} else {
					auth.addUser(p, p.getPassword());
				}

				// Commit the transaction
				ctx.commitTX();

				// Set JSP result
				result.setType(CommandResult.REQREDIRECT);
				result.setURL("/jsp/admin/activatePilotComplete.jsp");
			}
		} catch (SecurityException se) {
			ctx.rollbackTX();
			throw new CommandException(se);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Send e-mail notification
		Mailer mailer = new Mailer(ctx.getUser());
		mailer.setContext(mctx);
		mailer.send(p);

		// Set success flag
		result.setSuccess(true);
	}
}