// Copyright 2005, 2006, 2007, 2008, 2009, 2012, 2013, 2014, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.security;

import java.util.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.deltava.beans.*;
import org.deltava.beans.system.*;
import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.security.*;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to Authenticate users.
 * @author Luke
 * @version 6.0
 * @since 1.0
 */

public class LoginCommand extends AbstractCommand {

	private static final Logger log = Logger.getLogger(LoginCommand.class);

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occrurs. Login failures are not considered errors.
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command result
		CommandResult result = ctx.getResult();
		boolean isSecure = ctx.getRequest().isSecure();

		// If we're already logged in, just redirect to home
		boolean hasSSL = SystemData.getApp(SystemData.get("airline.code")).getSSL();
		if (ctx.isAuthenticated()) {
			result.setURL("home.do");
			result.setType(ResultType.REDIRECT);
			result.setSuccess(true);
			return;
		} else if (hasSSL && !isSecure && (ctx.getCookie("secureLogin") != null)) {
			HttpServletRequest req = ctx.getRequest();
			result.setURL("https://" + req.getServerName() + req.getRequestURI());
			result.setType(ResultType.REDIRECT);
			result.setSuccess(true);
			return;
		}

		// Determine where we are referring from, if on the site return back there
		String referer = ctx.getRequest().getHeader("Referer");
		if (!StringUtils.isEmpty(referer) && (!referer.contains("login"))) {
			try {
				URL url = new URL(referer);
				if (SystemData.get("airline.url").equalsIgnoreCase(url.getHost())) {
					if (isSecure && !"https".equals(url.getProtocol()))
						ctx.setAttribute("referTo", referer.replace("http:", "https:"), REQUEST);
					else
						ctx.setAttribute("referTo", referer, REQUEST);
				}
			} catch (MalformedURLException mue) {
				log.warn("Invalid HTTP referer - " + referer);
			}
		}

		// Get the names
		String fName = ctx.getParameter("firstName");
		String lName = ctx.getParameter("lastName");
		String code = ctx.getParameter("pilotCode");
		
		// Get pre-loaded names
		Cookie fnc = ctx.getCookie("dva_fname64");
		Cookie lnc = ctx.getCookie("dva_lname64");
		Cookie pcc = ctx.getCookie("dva_pCode");
		
		// Save first name
		Base64.Decoder b64d = Base64.getDecoder();
		if (fName != null)
			ctx.setAttribute("fname", fName, REQUEST);
		else if (fnc != null)
			ctx.setAttribute("fname", new String(b64d.decode(fnc.getValue()), StandardCharsets.UTF_8), REQUEST);
		
		// Save last name
		if (lName != null)
			ctx.setAttribute("lname", lName, REQUEST);
		else if (lnc != null)
			ctx.setAttribute("lname", new String(b64d.decode(lnc.getValue()), StandardCharsets.UTF_8), REQUEST);
		
		// Save pilot code
		if (code != null)
			ctx.setAttribute("pilotCode", code, REQUEST);
		else if (pcc != null)
			ctx.setAttribute("pilotCode", pcc.getValue(), REQUEST);

		// If we've got no firstName parameter, redirect to the login JSP
		if (fName == null) {
			result.setURL("/jsp/login.jsp");
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
		
		// Calculate screen size
		int screenX = StringUtils.parse(ctx.getParameter("screenX"), 1024);
		int screenY = StringUtils.parse(ctx.getParameter("screenY"), 768);
		int bodyX = StringUtils.parse(ctx.getParameter("bodyX"), 0);
		if ((bodyX > 800) && (bodyX < screenX))
			screenX = bodyX;

		// Build the full name
		StringBuilder fullName = new StringBuilder(fName.trim());
		fullName.append(' ');
		fullName.append(lName.trim());

		Pilot p = null;
		Date maxUserDate = UserPool.getMaxSizeDate();
		try {
			Connection con = ctx.getConnection();

			// Get the Pilot's Directory Name
			GetPilotDirectory dao = new GetPilotDirectory(con);
			List<Pilot> users = dao.getByName(fullName.toString(), SystemData.get("airline.db"));
			if (users.size() == 0) {
				log.warn("Unknown User Name - \"" + fullName + "\"");
				throw new SecurityException("Unknown User Name - \"" + fullName + "\"");
			} else if (users.size() > 1) {
				if (code != null) {
					try {
						int id = StringUtils.parseHex(code);
						p = dao.get(id);
					} catch (Exception e) {
						log.warn("Cannot parse pilot ID - " + code);
					}
				}
				
				// If we got more than one pilot, filter inactive pilots
				List<Pilot> activeUsers = new ArrayList<Pilot>();
				for (Pilot usr : users) {
					if ((usr.getStatus() == Pilot.ACTIVE) || (usr.getStatus() == Pilot.ON_LEAVE))
						activeUsers.add(usr);
				}
				
				// If there's more than one, we're good
				if ((p == null) && (activeUsers.size() == 1))
					p = activeUsers.get(0);
				else if (p == null) {
					ctx.setAttribute("dupeUsers", activeUsers, REQUEST);
					throw new SecurityException("Multiple Users found - please select");
				}
			} else
				p = users.get(0);

			// Get the authenticator and try to authenticate
			Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
			if (auth instanceof SQLAuthenticator) {
				SQLAuthenticator sqlAuth = (SQLAuthenticator) auth;
				sqlAuth.setConnection(con);
				sqlAuth.authenticate(p, ctx.getParameter("pwd"));
				sqlAuth.clearConnection();
			} else
				auth.authenticate(p, ctx.getParameter("pwd"));

			// If we're on leave, then reset the status (SetPilotLogin.login() will write it)
			boolean returnToActive = false;
			if (p.getStatus() == Pilot.ON_LEAVE) {
				log.info("Returning " + p.getName() + " from Leave");
				returnToActive = true;
				p.setStatus(Pilot.ACTIVE);
			} else if (p.getStatus() != Pilot.ACTIVE) {
				if (p.getStatus() == Pilot.SUSPENDED) {
					log.warn(p.getName() + " status = Suspended, setting cookie");
					Cookie wc = new Cookie("dvaAuthStatus", StringUtils.formatHex(p.getID()));
					wc.setPath("/");
					wc.setMaxAge(86400);
					ctx.getResponse().addCookie(wc);
				} else
					log.warn(p.getName() + " status = " + p.getStatusName());
				
				throw new SecurityException("You are not an Active Pilot at " + SystemData.get("airline.name"));
			}

			// Load online/ACARS totals
			GetFlightReports frdao = new GetFlightReports(con);
			frdao.getOnlineTotals(p, SystemData.get("airline.db"));
			
			// Get IP address info
			String remoteAddr = ctx.getRequest().getRemoteAddr();
			GetIPLocation ipdao = new GetIPLocation(con);
			IPBlock addrInfo = ipdao.get(remoteAddr);

			// Create the user authentication cookie
			SecurityCookieData cData = new SecurityCookieData(p.getHexID());
			cData.setLoginDate(System.currentTimeMillis());
			cData.setRemoteAddr(remoteAddr);
			cData.setScreenSize(screenX, screenY);
			
			// Encode the encrypted data via Base64
			Cookie c = new Cookie(CommandContext.AUTH_COOKIE_NAME, SecurityCookieGenerator.getCookieData(cData));
			c.setMaxAge(-1);
			c.setHttpOnly(true);
			c.setSecure(isSecure);
			c.setPath("/");
			ctx.getResponse().addCookie(c);
			
			// Set secure only cookie if requested
			boolean doSecureLogin = Boolean.valueOf(ctx.getParameter("secureLogin")).booleanValue();
			if (doSecureLogin) {
				c = new Cookie("secureLogin", "1");
				c.setHttpOnly(true);
				c.setPath(ctx.getRequest().getRequestURI());
				c.setMaxAge(365 * 86400);
				ctx.getResponse().addCookie(c);
			} else if (isSecure) {
				c = new Cookie("secureLogin", "");
				c.setMaxAge(0);
				ctx.getResponse().addCookie(c);
			}

			// Check if we have an address validation entry outstanding
			GetAddressValidation avdao = new GetAddressValidation(con);
			AddressValidation av = avdao.get(p.getID());
			
			// Unblock the user if blocked
			UserPool.unblock(p);

			// Start the transaction
			ctx.startTX();

			// Save login time and hostname
			SetPilotLogin wdao = new SetPilotLogin(con);
			p.setLastLogin(new Date());
			p.setLoginHost(ctx.getRequest().getRemoteHost());
			wdao.login(p.getID(), p.getLoginHost());
			
			// Save login hostname/IP address forever
			SetSystemData sysdao = new SetSystemData(con);
			sysdao.login(SystemData.get("airline.db"), p.getID(), remoteAddr, p.getLoginHost());
			
			// Clear LOA if done today
			SetStatusUpdate sudao = new SetStatusUpdate(con);
			if (returnToActive)
				sudao.clearLOA(p.getID());

			// Check if we've surpassed the notificaton interval
			if (returnToActive) {
				long interval = (System.currentTimeMillis() - p.getLastLogin().getTime()) / 1000;
				if ((interval / 86400) < SystemData.getInt("users.notify_days", 30))
					returnToActive = false;
			}

			// Mark as returned from leave
			if (returnToActive) {
				StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.STATUS_CHANGE);
				upd.setAuthorID(p.getID());
				upd.setDescription("Returned from Leave of Absence");
				sudao.write(upd);
			}

			// Clear the inactivity interval (if any)
			SetInactivity idao = new SetInactivity(con);
			idao.delete(p.getID());

			// Create the session and stuff in the pilot data
			String userAgent = ctx.getRequest().getHeader("user-agent");
			HttpSession s = ctx.getRequest().getSession(true);
			s.setAttribute("java.util.Locale", Locale.US);
			s.setAttribute(HTTPContext.USER_ATTR_NAME, p);
			s.setAttribute(HTTPContext.ADDRINFO_ATTR_NAME, addrInfo);
			s.setAttribute(HTTPContext.USERAGENT_ATTR_NAME, userAgent);
			s.setAttribute(HTTPContext.SSL_ATTR_NAME, Boolean.valueOf(ctx.getRequest().isSecure()));
			s.setAttribute(CommandContext.AUTH_COOKIE_NAME, cData);
			s.setAttribute(CommandContext.SCREENX_ATTR_NAME, Integer.valueOf(cData.getScreenX()));
			s.setAttribute(CommandContext.SCREENY_ATTR_NAME, Integer.valueOf(cData.getScreenY()));
			
			// Determine where we are referring from, if on the site return back there
			if (av != null) {
				log.info("Invalidated e-mail address for " + p.getName());
				s.setAttribute("addr", av);
				s.setAttribute("next_url", "validate.do");
			} else if (!StringUtils.isEmpty(ctx.getParameter("redirectTo")))
				s.setAttribute("next_url", ctx.getParameter("redirectTo"));
			else
				s.setAttribute("next_url", "home.do");

			// Add the user to the User pool
			UserPool.add(p, s.getId(), addrInfo, userAgent, ctx.getRequest().isSecure());
			
			// Check if we need to save maximum users
			if (UserPool.getMaxSizeDate().after(maxUserDate)) {
				String prefix = SystemData.get("airline.code").toLowerCase();
				SetMetadata mdwdao = new SetMetadata(con);
				mdwdao.write(prefix + ".users.max.count", String.valueOf(UserPool.getMaxSize()));
				mdwdao.write(prefix + ".users.max.date", StringUtils.format(UserPool.getMaxSizeDate(), "MM/dd/yyyy HH:mm"));
			}

			// Commit the transaction
			ctx.commitTX();
		} catch (SecurityException se) {
			ctx.release();
			result.setURL("/jsp/login.jsp");
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
			Base64.Encoder b64e = Base64.getEncoder();
			int cookieAge = SystemData.getInt("users.user_cookie_age") * 86400;

			fnc = new Cookie("dva_fname64", b64e.encodeToString(p.getFirstName().getBytes(StandardCharsets.UTF_8)));
			fnc.setMaxAge(cookieAge);
			ctx.getResponse().addCookie(fnc);

			lnc = new Cookie("dva_lname64", b64e.encodeToString(p.getLastName().getBytes(StandardCharsets.UTF_8)));
			lnc.setMaxAge(cookieAge);
			ctx.getResponse().addCookie(lnc);
			
			pcc = new Cookie("dva_pCode", p.getHexID());
			pcc.setMaxAge(cookieAge);
			ctx.getResponse().addCookie(pcc);
		} else {
			fnc = new Cookie("dva_fname64", "");
			fnc.setMaxAge(0);
			ctx.getResponse().addCookie(fnc);

			lnc = new Cookie("dva_lname64", "");
			lnc.setMaxAge(0);
			ctx.getResponse().addCookie(lnc);
			
			pcc = new Cookie("dva_pCode", "");
			pcc.setMaxAge(0);
			ctx.getResponse().addCookie(pcc);
		}
		
		// Clear warning cookie if valid
		if (ctx.getCookie("dvaAuthStatus") != null) {
			log.warn("Resetting Suspended warning cookie for " + p.getName());
			Cookie wc = new Cookie("dvaAuthStatus", "");
			wc.setMaxAge(0);
			ctx.getResponse().addCookie(wc);
		}
		
		// Mark us as complete
		result.setURL("cookieCheck.do");
		result.setType(ResultType.REDIRECT);
		result.setSuccess(true);
	}
}