// Copyright 2005, 2006 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.commands.security;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;

import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;
import org.deltava.beans.StatusUpdate;
import org.deltava.beans.system.AddressValidation;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.security.*;

import org.deltava.util.StringUtils;
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

		// Determine where we are referring from, if on the site return back there
		String referer = ctx.getRequest().getHeader("Referer");
		if (!StringUtils.isEmpty(referer)) {
			try {
				URL url = new URL(referer);
				if (SystemData.get("airline.url").equalsIgnoreCase(url.getHost()))
					ctx.setAttribute("referTo", referer, REQUEST);
			} catch (MalformedURLException mue) {
				log.warn("Invalid HTTP referer - " + referer);
			}
		}

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
			result.setURL("/jsp/error/jsDisabled.jsp");
			result.setSuccess(true);
			return;
		}

		Pilot p = null;
		try {
			Connection con = ctx.getConnection();

			// Get the Pilot's Directory Name
			GetPilotDirectory dao = new GetPilotDirectory(con);
			p = dao.getByName(fName + " " + lName, SystemData.get("airline.db"));
			if (p == null)
				throw new SecurityException("Unknown User Name");

			// Get the authenticator and try to authenticate
			Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
			auth.authenticate(p, ctx.getParameter("pwd"));

			// If we're on leave, then reset the status (SetPilotLogin.login() will write it)
			boolean returnToActive = false;
			if (p.getStatus() == Pilot.ON_LEAVE) {
				log.info("Returning " + p.getName() + " from Leave");
				returnToActive = true;
				p.setStatus(Pilot.ACTIVE);
			} else if (p.getStatus() != Pilot.ACTIVE) {
				log.warn(p.getName() + " status = " + Pilot.STATUS[p.getStatus()]);
				throw new SecurityException("You are not an Active Pilot at " + SystemData.get("airline.name"));
			}

			// Log the pilot in
			p.login(ctx.getRequest().getRemoteHost());

			// Create the user authentication cookie
			SecurityCookieData cData = new SecurityCookieData(p.getDN());
			cData.setPassword(ctx.getParameter("pwd"));
			cData.setRemoteAddr(ctx.getRequest().getRemoteAddr());
			ctx.getResponse().addCookie(SecurityCookieGenerator.getCookie(CommandContext.AUTH_COOKIE_NAME, cData));

			// Save screen resolution
			try {
				cData.setScreenSize(Integer.parseInt(ctx.getParameter("screenX")), Integer.parseInt(ctx
						.getParameter("screenY")));
			} catch (NumberFormatException nfe) {
				cData.setScreenSize(1024, 768);
			}

			// Check if we have an address validation entry outstanding
			GetAddressValidation avdao = new GetAddressValidation(con);
			AddressValidation av = avdao.get(p.getID());

			// Start the transaction
			ctx.startTX();

			// Save login time and hostname
			SetPilotLogin wdao = new SetPilotLogin(con);
			wdao.login(p);

			// Mark as returned from leave
			if (returnToActive) {
				SetStatusUpdate sudao = new SetStatusUpdate(con);
				StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.STATUS_CHANGE);
				upd.setAuthorID(p.getID());
				upd.setDescription("Returned from Leave of Absence");
				sudao.write(upd);
			}

			// Clear the inactivity interval (if any)
			SetInactivity idao = new SetInactivity(con);
			idao.delete(p.getID());

			// Create the session and stuff in the pilot data
			HttpSession s = ctx.getRequest().getSession(true);
			s.setAttribute(CommandContext.USER_ATTR_NAME, p);
			s.setAttribute(CommandContext.SCREENX_ATTR_NAME, new Integer(cData.getScreenX()));
			s.setAttribute(CommandContext.SCREENY_ATTR_NAME, new Integer(cData.getScreenY()));

			// Determine where we are referring from, if on the site return back there
			if (av != null) {
				log.info("Invalidated e-mail address for " + p.getName());
				s.setAttribute(CommandContext.ADDRINVALID_ATTR_NAME, Boolean.TRUE);
				s.setAttribute("next_url", "pilotcenter.do");
			} else if (!StringUtils.isEmpty(ctx.getParameter("redirectTo")))
				s.setAttribute("next_url", ctx.getParameter("redirectTo"));
			else
				s.setAttribute("next_url", "home.do");

			// Add the user to the User pool
			UserPool.add(p, s.getId());

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

		// Check if we are going to save the first/last names
		boolean saveName = Boolean.valueOf(ctx.getParameter("saveInfo")).booleanValue();
		if (saveName) {
			int cookieAge = SystemData.getInt("users.user_cookie_age") * 86400;

			Cookie fnc = new Cookie("dva_fname", p.getFirstName());
			fnc.setDomain(ctx.getRequest().getServerName());
			fnc.setMaxAge(cookieAge);
			fnc.setPath("/login.do");
			ctx.getResponse().addCookie(fnc);

			Cookie lnc = new Cookie("dva_lname", p.getLastName());
			lnc.setDomain(ctx.getRequest().getServerName());
			lnc.setMaxAge(cookieAge);
			lnc.setPath("/login.do");
			ctx.getResponse().addCookie(lnc);
		} else {
			Cookie fnc = new Cookie("dva_fname", "");
			fnc.setMaxAge(0);
			ctx.getResponse().addCookie(fnc);

			Cookie lnc = new Cookie("dva_lname", "");
			lnc.setMaxAge(0);
			ctx.getResponse().addCookie(lnc);
		}

		// Mark us as complete
		result.setURL("cookieCheck.do");
		result.setType(CommandResult.REDIRECT);
		result.setSuccess(true);
	}
}