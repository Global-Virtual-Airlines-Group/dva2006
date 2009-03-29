// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.security;

import java.util.List;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.Authenticator;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to reset users' passwords.
 * @author Luke
 * @version 2.3
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
		String code = ctx.getParameter("pilotCode");

		// If no name found, then redirect to the page
		if (fName == null) {
			result.setSuccess(true);
			return;
		}
		
		// Build the name
		StringBuilder buf = new StringBuilder(fName);
		buf.append(' ');
		buf.append(lName);

		// Create the messaging context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());

		// Get the User object - you cannot reset a staff member's password
		Pilot usr = null;
		try {
			Connection con = ctx.getConnection();

			// Get the Directory name
			GetPilot dao = new GetPilot(con);
			List<Pilot> users = dao.getByName(buf.toString(), SystemData.get("airline.db"));
			if (users.size() == 0) {
				ctx.setMessage("User " + buf.toString() + " not found");
				ctx.release();
				return;
			} else if (users.size() > 1) {
				if (code != null) {
					try {
						int id = StringUtils.parseHex(code);
						usr = dao.get(id);
					} catch (Exception e) {
						log.warn("Cannot parse pilot ID - " + code);
					}
				}
				
				if (usr == null) {
					ctx.setAttribute("userName", buf.toString(), REQUEST);
					ctx.setAttribute("dupeUsers", users, REQUEST);
					ctx.release();
					return;
				}
			} else
				usr = users.get(0);

			// Save in the message context
			mctxt.addData("pilot", usr);

			// Load the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("PWDRESET"));
		} catch (DAOException de) {
			ctx.release();
			ctx.setMessage(de.getMessage());
			return;
		} finally {
			ctx.release();
		}

		// Check user status
		if ((usr.getStatus() != Pilot.ACTIVE) && (usr.getStatus() != Pilot.ON_LEAVE)) {
			ctx.setMessage("Cannot reset password for " + usr.getName() + "(" + usr.getPilotCode() + ") - status=" + usr.getStatusName());
			return;
		}

		// Check the email-address and roles; if we have more than one we cannot be reset
		if (!ctx.isUserInRole("HR")) {
			if (usr.getRoles().size() > 1) {
				ctx.setMessage("Cannot reset password for " + usr.getName() + " (" + usr.getPilotCode() + ") - roles=" + usr.getRoles().size());
				return;
			} else if (!usr.getEmail().equalsIgnoreCase(ctx.getParameter("eMail"))) {
				ctx.setMessage("Cannot reset password for " + usr.getName() + " - Invalid E-Mail Address");
				return;
			}
		}

		// Generate a new password for the user and save the user in the request
		String newPwd = PasswordGenerator.generate(8);
		usr.setPassword(newPwd);
		ctx.setAttribute("pilot", usr, REQUEST);

		// Get the authenticator and update the password
		try {
			Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
			if (auth.contains(usr))
				auth.updatePassword(usr, newPwd);
			else {
				log.warn(usr.getName() + " not found, adding");
				auth.add(usr, newPwd);
			}
			
			// Validate the password
			auth.authenticate(usr, newPwd);
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
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}