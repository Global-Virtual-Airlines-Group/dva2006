package org.deltava.commands.security;

import java.sql.Connection;

import org.deltava.beans.Pilot;

import org.deltava.commands.*;
import org.deltava.mail.*;

import org.deltava.dao.GetPilotDirectory;
import org.deltava.dao.GetMessageTemplate;
import org.deltava.dao.DAOException;

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

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Initialize default command result
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/passwordReset.jsp");

		boolean isAuthorized = ctx.getRequest().isUserInRole("HR");
		if (ctx.isAuthenticated() && !isAuthorized)
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

		// Get the User object - you cannot reset a staff member's password
		String dName = null;
		Pilot usr = null;
		try {
			Connection con = ctx.getConnection();

			// Get the Directory name
			GetPilotDirectory dao = new GetPilotDirectory(con);
			dName = dao.getDirectoryName(fName, lName);
			if (dName == null) {
				ctx.setMessage("User " + fName + " " + lName + " not found");
				ctx.release();
				return;
			}

			// Load the pilot and save in the message context
			usr = dao.getFromDirectory(dName);
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
		if (!isAuthorized) {
			if (!usr.getEmail().equalsIgnoreCase(ctx.getParameter("eMail"))) {
				ctx.setMessage("Cannot reset password for " + usr.getName() + " - Invalid E-Mail Address");
				return;
			}
		}

		// Generate a new password for the user and save the user in the request
		String newPwd = PasswordGenerator.generate(10);
		usr.setPassword(newPwd);
		ctx.setAttribute("user", usr, REQUEST);

		// Get the authenticator and update the password
		Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
		try {
			auth.updatePassword(dName, newPwd);
		} catch (SecurityException se) {
			ctx.setMessage("Error updating password for " + dName + " - " + se.getMessage());
			return;
		}

		// Send a notification message
		Mailer mailer = new Mailer(ctx.getUser());
		mailer.setContext(mctxt);
		mailer.send(usr);

		// Forward to JSP
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/pilot/passwordResetComplete.jsp");
		result.setSuccess(true);
	}
}