// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.security;

import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.Authenticator;

import org.deltava.util.PasswordGenerator;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to reset users' passwords.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PasswordResetCommand extends AbstractCommand {
	
	private static final Logger log = Logger.getLogger(PasswordResetCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Initialize default command result
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/passwordReset.jsp");
		if (ctx.isAuthenticated() && !ctx.isUserInRole("HR"))
			throw securityException("Not Authorized");

		// Check for first/last name
		String fName = ctx.getParameter("fName");
		String lName = ctx.getParameter("lName");

		// If no name found, then redirect to the page
		if (fName == null) {
			result.setSuccess(true);
			return;
		}

		// Create the messaging context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());

		// Get the User object - you cannot reset a staff member's password
		Pilot usr = null;
		try {
			Connection con = ctx.getConnection();

			// Get the Directory name
			GetPilot dao = new GetPilot(con);
			usr = dao.getByName(fName + " " + lName, SystemData.get("airline.db"));
			if (usr == null) {
				ctx.setMessage("User " + fName + " " + lName + " not found");
				ctx.release();
				return;
			}

			// Save in the message context
			mctxt.addData("pilot", usr);

			// Load the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("PWDRESET"));
		} catch (DAOException de) {
			ctx.setMessage(de.getMessage());
			return;
		} finally {
			ctx.release();
		}

		// Check to make sure the user exists in the database
		if (usr == null) {
			ctx.setMessage("User " + fName + " " + lName + " not found in database");
			return;
		}

		// Check user status
		if ((usr.getStatus() != Pilot.ACTIVE) && (usr.getStatus() != Pilot.ON_LEAVE)) {
			ctx.setMessage("Cannot reset password for " + usr.getName() + " - status=" + usr.getStatusName());
			return;
		}

		// Check the roles; if we have more than one we cannot be reset
		if (usr.getRoles().size() > 1) {
			ctx.setMessage("Cannot reset password for " + usr.getName() + " - roles=" + usr.getRoles().size());
			return;
		}

		// Check the e-mail address
		if (!ctx.isUserInRole("HR")) {
			if (!usr.getEmail().equalsIgnoreCase(ctx.getParameter("eMail"))) {
				ctx.setMessage("Cannot reset password for " + usr.getName() + " - Invalid E-Mail Address");
				return;
			}
		}

		// Generate a new password for the user and save the user in the request
		String newPwd = PasswordGenerator.generate(10);
		usr.setPassword(newPwd);
		ctx.setAttribute("pilot", usr, REQUEST);

		// Get the authenticator and update the password
		try {
			Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
			if (auth.contains(usr))
				auth.updatePassword(usr, newPwd);
			else {
				log.warn(usr.getName() + " not found, adding");
				auth.addUser(usr, newPwd);
			}
		} catch (SecurityException se) {
			ctx.setMessage("Error updating password for " + usr.getDN() + " - " + se.getMessage());
			return;
		}

		// Generate an HTTP session if one doesn't exist
		ctx.getRequest().getSession(true);

		// Send a notification message
		Mailer mailer = new Mailer(ctx.getUser());
		mailer.setContext(mctxt);
		mailer.send(usr);

		// Forward to JSP
		result.setURL("/jsp/pilot/passwordResetComplete.jsp");
		result.setType(CommandResult.REQREDIRECT);
		result.setSuccess(true);
	}
}