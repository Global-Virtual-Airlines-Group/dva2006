// Copyright 2005, 2006, 2007, 2008, 2009, 2012, 2013, 2014, 2015, 2016, 2018, 2019, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.security;

import java.net.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.stats.*;
import org.deltava.beans.stats.AccomplishmentHistoryHelper.Result;
import org.deltava.beans.system.*;
import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.security.*;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to Authenticate users.
 * @author Luke
 * @version 10.0
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
		
		// Determine where we are referring from, if on the site return back there
		String referer = ctx.getRequest().getHeader("Referer");
		if (!StringUtils.isEmpty(referer) && (!referer.contains("login"))) {
			try {
				URL url = new URL(referer);
				if (SystemData.get("airline.url").equalsIgnoreCase(url.getHost()))
					ctx.setAttribute("referTo", referer, REQUEST);
			} catch (MalformedURLException mue) {
				log.warn("Invalid HTTP referer - " + referer);
				referer = null;
			}
		}

		// If we're already logged in, just redirect to home
		if (ctx.isAuthenticated()) {
			result.setURL(StringUtils.isEmpty(referer) ? "home.do" : referer);
			result.setType(ResultType.REDIRECT);
			result.setSuccess(true);
			return;
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
		
		// Build the full name
		StringBuilder fullName = new StringBuilder(fName.trim());
		fullName.append(' ');
		fullName.append(String.valueOf(lName).trim());

		Pilot p = null;
		Instant maxUserDate = UserPool.getMaxSizeDate();
		try {
			Connection con = ctx.getConnection();

			// Get the Pilot's Directory Name
			GetPilotDirectory dao = new GetPilotDirectory(con);
			List<Pilot> users = dao.getByName(fullName.toString(), ctx.getDB()).stream().filter(usr -> !usr.getIsForgotten()).collect(Collectors.toList());
			if (users.size() == 0) {
				String msg = String.format("Unknown User Name - \"%s\"", fullName);
				log.warn(msg);
				throw new SecurityException(msg);
			} else if (users.size() > 1) {
				if (code != null) {
					try {
						p = dao.get(StringUtils.parseHex(code));
					} catch (Exception e) {
						log.warn(String.format("Cannot parse pilot ID - %s", code));
					}
				}
				
				// If we got more than one pilot, filter inactive pilots
				List<Pilot> activeUsers = users.stream().filter(usr -> ((usr.getStatus() == PilotStatus.ACTIVE) || (usr.getStatus() == PilotStatus.ONLEAVE))).collect(Collectors.toList());
				
				// If there's more than one, we're good
				if ((p == null) && (activeUsers.size() == 1))
					p = activeUsers.get(0);
				else if (p == null) {
					ctx.setAttribute("dupeUsers", activeUsers, REQUEST);
					throw new SecurityException("Multiple Users found - please select");
				}
			} else
				p = users.get(0);
			
			// Check the blacklist
			String remoteAddr = ctx.getRequest().getRemoteAddr();
			GetSystemData sysdao = new GetSystemData(con);
			BlacklistEntry be = sysdao.getBlacklist(remoteAddr);
			if (be != null)
				throw new SecurityException(String.format("Login prohibited from %s", be));
			
			// Get the authenticator and try to authenticate
			try (org.deltava.security.Authenticator auth = (org.deltava.security.Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR)) {
				if (auth instanceof SQLAuthenticator) ((SQLAuthenticator) auth).setConnection(con);
				auth.authenticate(p, ctx.getParameter("pwd"));
			}

			// If we're on leave, then reset the status (SetPilotLogin.login() will write it)
			boolean returnToActive = false;
			if (p.getStatus() == PilotStatus.ONLEAVE) {
				log.info(String.format("Returning %s from Leave", p.getName()));
				returnToActive = true;
				p.setStatus(PilotStatus.ACTIVE);
			} else if (p.getStatus() != PilotStatus.ACTIVE) {
				if (p.getStatus() == PilotStatus.SUSPENDED) {
					log.warn(String.format("%s status = Suspended, setting cookie", p.getName()));
					Cookie wc = new Cookie("dvaAuthStatus", StringUtils.formatHex(p.getID()));
					wc.setPath("/");
					wc.setMaxAge(86400 * 180);
					ctx.addCookie(wc);
				} else
					log.warn(String.format("%s status = %s", p.getName(), p.getStatus().getDescription()));
				
				throw new SecurityException(String.format("You are not an Active Pilot at %s", SystemData.get("airline.name")));
			}
			
			// Calculate anniversary accomplishments
			Collection<DatedAccomplishment> pAnvAccs = new ArrayList<DatedAccomplishment>();
			GetAccomplishment acmdao = new GetAccomplishment(con);
			List<Accomplishment> anvAccs = acmdao.getByUnit(AccomplishUnit.MEMBERDAYS);
			AccomplishmentHistoryHelper accHelper = new AccomplishmentHistoryHelper(p);
			List<DatedAccomplishment> pAccs = new ArrayList<DatedAccomplishment>(acmdao.getByPilot(p, ctx.getDB()));
			anvAccs.removeIf(acc -> pAccs.contains(acc));
			for (Accomplishment acc : anvAccs) {
				if (accHelper.has(acc) != Result.NOTYET) {
					DatedAccomplishment da = new DatedAccomplishment(p.getID(), accHelper.achieved(acc), acc);
					pAnvAccs.add(da);
				}
			}

			// Load online/ACARS totals
			GetFlightReports frdao = new GetFlightReports(con);
			frdao.getOnlineTotals(p, ctx.getDB());
			
			// Get IP address info
			GetIPLocation ipdao = new GetIPLocation(con);
			IPBlock addrInfo = ipdao.get(remoteAddr);

			// Create the user authentication cookie
			SecurityCookieData cData = new SecurityCookieData(p.getHexID());
			cData.setLoginDate(Instant.now());
			cData.setRemoteAddr(remoteAddr);
			
			// Encode the encrypted data via Base64
			Cookie c = new Cookie(CommandContext.AUTH_COOKIE_NAME, SecurityCookieGenerator.getCookieData(cData));
			c.setMaxAge(-1);
			c.setHttpOnly(true);
			c.setSecure(true);
			c.setPath("/");
			ctx.addCookie(c);
			
			// Check if we have an address validation entry outstanding
			GetAddressValidation avdao = new GetAddressValidation(con);
			AddressValidation av = avdao.get(p.getID());
			
			// Unblock the user if blocked
			UserPool.unblock(p);

			// Start the transaction
			ctx.startTX();

			// Save login time and hostname
			SetPilotLogin wdao = new SetPilotLogin(con);
			p.setLastLogin(Instant.now());
			p.setLoginHost(ctx.getRequest().getRemoteHost());
			wdao.login(p.getID(), p.getLoginHost(), ctx.getDB());
			
			// Save login hostname/IP address forever
			SetSystemData syswdao = new SetSystemData(con);
			syswdao.login(ctx.getDB(), p.getID(), remoteAddr, p.getLoginHost());
			
			// Write accomplishments
			SetAccomplishment accwdao = new SetAccomplishment(con);
			for (DatedAccomplishment da : pAnvAccs)
				accwdao.achieve(da.getPilotID(), da, da.getDate());
			
			// Clear LOA if done today
			SetStatusUpdate sudao = new SetStatusUpdate(con);
			if (returnToActive)
				sudao.clearLOA(p.getID());

			// Check if we've surpassed the notificaton interval
			if (returnToActive) {
				Duration d = Duration.between(p.getLastLogin(), Instant.now());
				if (d.toDays() < SystemData.getInt("users.notify_days", 30))
					returnToActive = false;
			}

			// Mark as returned from leave
			if (returnToActive) {
				StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.STATUS_CHANGE);
				upd.setAuthorID(p.getID());
				upd.setDescription("Returned from Leave of Absence");
				sudao.write(upd, ctx.getDB());
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
			s.setAttribute(CommandContext.AUTH_COOKIE_NAME, cData);
			
			// Determine where we are referring from, if on the site return back there
			if (av != null) {
				log.info(String.format("Invalidated e-mail address for %s", p.getName()));
				s.setAttribute("addr", av);
				s.setAttribute("next_url", "validate.do");
			} else if (!StringUtils.isEmpty(ctx.getParameter("redirectTo")))
				s.setAttribute("next_url", ctx.getParameter("redirectTo"));
			else
				s.setAttribute("next_url", "home.do");

			// Add the user to the User pool
			UserPool.add(p, s.getId(), addrInfo, userAgent);
			
			// Check if we need to save maximum users
			if ((maxUserDate == null) || UserPool.getMaxSizeDate().isAfter(maxUserDate)) {
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
			ctx.addCookie(fnc);

			lnc = new Cookie("dva_lname64", b64e.encodeToString(p.getLastName().getBytes(StandardCharsets.UTF_8)));
			lnc.setMaxAge(cookieAge);
			ctx.addCookie(lnc);
			
			pcc = new Cookie("dva_pCode", p.getHexID());
			pcc.setMaxAge(cookieAge);
			ctx.addCookie(pcc);
		} else {
			fnc = new Cookie("dva_fname64", "");
			fnc.setMaxAge(0);
			ctx.addCookie(fnc);

			lnc = new Cookie("dva_lname64", "");
			lnc.setMaxAge(0);
			ctx.addCookie(lnc);
			
			pcc = new Cookie("dva_pCode", "");
			pcc.setMaxAge(0);
			ctx.addCookie(pcc);
		}
		
		// Clear warning cookie if valid
		if (ctx.getCookie("dvaAuthStatus") != null) {
			log.warn(String.format("Resetting Suspended warning cookie for %s", p.getName()));
			Cookie wc = new Cookie("dvaAuthStatus", "");
			wc.setMaxAge(0);
			ctx.addCookie(wc);
		}
		
		// Mark us as complete
		result.setURL("cookieCheck.do");
		result.setType(ResultType.REDIRECT);
		result.setSuccess(true);
	}
}