package org.deltava.commands.security;

import java.sql.Connection;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.security.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to Authenticate users.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class LoginCommand extends AbstractCommand {

	private static final Logger log = Logger.getLogger(LoginCommand.class);

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occrurs. Login failures are not considered errors.
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command result and set default result URL
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/login.jsp");

		// Get the names
		String fName = ctx.getParameter("firstName");
		String lName = ctx.getParameter("lastName");

		// If we've got no firstName parameter, redirect to the login JSP
		if (fName == null) {
			result.setSuccess(true);
			return;
		}
		
		// Check that JavaScript is working properly
		boolean jsOK = Boolean.valueOf(ctx.getParameter("jsOK")).booleanValue();
		if (!jsOK) {
			result.setURL("/jsp/jsDisabled.jsp");
			result.setSuccess(true);
			return;
		}

		Pilot p = null;
		try {
			Connection con = ctx.getConnection();

			// Get the Pilot's Directory Name
			GetPilotDirectory dao = new GetPilotDirectory(con);
			String dN = dao.getDirectoryName(fName, lName);
			if (dN == null)
				throw new SecurityException("Unknown User Name");

			// Get the authenticator and try to authenticate
			log.debug("Authenticating " + dN);
			Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
			auth.authenticate(dN, ctx.getParameter("pwd"));

			// If we authenticated, load the pilot
			p = dao.getFromDirectory(dN);
			if (p == null)
				throw new DAOException("Error loading Pilot " + dN);
			
			// If we're on leave, then reset the status (SetPilotLogin.login() will write it)
			if (p.getStatus() == Pilot.ON_LEAVE) {
			   log.info("Returning " + p.getName() + " from Leave");
			   p.setStatus(Pilot.ACTIVE);
			} else if (p.getStatus() != Pilot.ACTIVE) {
			   log.warn(p.getName() + " status = " + Pilot.STATUS[p.getStatus()]);
			   throw new SecurityException("You are not an Active Pilot at " + SystemData.get("airline.name"));
			}

			// Log the pilot in
			p.login(ctx.getRequest().getRemoteHost());
			
			// Start the transaction
			ctx.startTX();

			// Save login time and hostname
			SetPilotLogin wdao = new SetPilotLogin(con);
			wdao.login(p);
			
			// Clear the inactivity interval (if any)
			SetInactivity idao = new SetInactivity(con);
			idao.delete(p.getID());
			
			// Create the session and stuff in the pilot data
			HttpSession s = ctx.getRequest().getSession(true);
			s.setAttribute(CommandContext.USER_ATTR_NAME, p);

			// Add the user to the User pool
			UserPool.addPerson(p, s.getId());

			// Update the session data
			SetSystemData swdao = new SetSystemData(con);
			swdao.updateSession(s.getId(), p, ctx.getRequest().getRemoteAddr(), ctx.getRequest().getRemoteHost());
			
			// Commit the transaction
			ctx.commitTX();
		} catch (SecurityException se) {
			ctx.release();
			ctx.setMessage(se.getMessage());
			return;
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Create the user authentication cookie
		SecurityCookieData cData = new SecurityCookieData(p.getDN());
		cData.setPassword(ctx.getParameter("pwd"));
		cData.setRemoteAddr(ctx.getRequest().getRemoteAddr());
		ctx.getResponse().addCookie(SecurityCookieGenerator.getCookie(CommandContext.AUTH_COOKIE_NAME, cData));

		// Set the next URL after the CookieCheck command
		ctx.setAttribute("next_url", "home.do", SESSION);

		// Check if we are going to save the first/last names
		boolean saveName = Boolean.valueOf(ctx.getParameter("saveInfo")).booleanValue();
		if (saveName) {
			int cookieAge = SystemData.getInt("users.user_cookie_age") * 86400;

			Cookie fnc = new Cookie("dva_fname", p.getFirstName());
			fnc.setVersion(1);
			fnc.setMaxAge(cookieAge);
			fnc.setDomain(ctx.getRequest().getServerName());
			//fnc.setPath("/login.do");
			ctx.getResponse().addCookie(fnc);

			Cookie lnc = new Cookie("dva_lname", p.getLastName());
			lnc.setVersion(1);
			lnc.setMaxAge(cookieAge);
			lnc.setDomain(ctx.getRequest().getServerName());
			// lnc.setPath("/login.do");
			ctx.getResponse().addCookie(lnc);
		}

		// Mark us as complete
		result.setURL("cookieCheck.do");
		result.setType(CommandResult.REDIRECT);
		result.setSuccess(true);
	}
}