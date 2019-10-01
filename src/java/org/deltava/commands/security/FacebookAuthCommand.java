// Copyright 2010, 2012, 2015, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.security;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.fb.*;

import org.deltava.commands.*;

import org.deltava.dao.*;
import org.deltava.dao.http.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

import org.gvagroup.common.SharedData;

/**
 * A Web Site Command to handle Facebook authorization commands.
 * @author Luke
 * @version 8.7
 * @since 3.4
 */

public class FacebookAuthCommand extends AbstractCommand {
	
	private static final Logger log = Logger.getLogger(FacebookAuthCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occrurs.
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the error reason
		CommandResult result = ctx.getResult();
		String errorReason = ctx.getParameter("error_reason");
		if (!StringUtils.isEmpty(errorReason)) {
			ctx.setMessage(errorReason);
			result.setURL("/jsp/error/fbAuthError.jsp");
			result.setSuccess(true);
			return;
		}
		
		// Get the code
		String code = ctx.getParameter("code");
		if (StringUtils.isEmpty(code)) {
			ctx.setMessage("No authorization code returned from Facebook");
			result.setURL("/jsp/error/fbAuthError.jsp");
			result.setSuccess(true);
			return;
		}
		
		try {
			Connection con = ctx.getConnection();
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(ctx.getUser().getID());
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + ctx.getUser().getID());
		
			// Fetch an authorization token
			GetFacebookAuth fadao = new GetFacebookAuth();
			fadao.setAppID(SystemData.get("users.facebook.id"));
			fadao.setSecret(SystemData.get("users.facebook.secret"));
			String token = fadao.getAccessToken(code, ctx.getRequest().getRequestURL().toString());
			if (ctx.isUserInRole("Facebook"))
				ctx.setAttribute("fbToken", token, REQUEST);
			
			// If the token is really big, log it
			if (token.length() > 240)
				log.warn("Long FB token for " + p.getName() + " - " + token);
			
			// Get the user's information
			GetFacebookData fbdao = new GetFacebookData();
			fbdao.setToken(token);
			ProfileInfo info = fbdao.getUserInfo();
			
			// Exchange the short-term token for a long-term token
			fadao.reset();
			fadao.setToken(token);
			fadao.setReturnErrorStream(true);
			String longToken = fadao.getLongLifeToken();
			
			// Update the user
			p.setIMHandle(IMAddress.FB, info.getID());
			p.setIMHandle(IMAddress.FBTOKEN, longToken);
			
			// Create status update
			Collection<StatusUpdate> upds = new ArrayList<StatusUpdate>();
			StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.EXT_AUTH);
			upd.setDescription("Facebook Authorized");
			upd.setAuthorID(p.getID());
			upds.add(upd);

			// Get page authentication data
			if (ctx.isUserInRole("Facebook")) {
				fbdao.reset();
				String appToken = fbdao.getPageToken();
				if (!StringUtils.isEmpty(appToken)) {
					p.setIMHandle(IMAddress.FBPAGE, appToken);
					ctx.setAttribute("fbPageAuth", Boolean.TRUE, REQUEST);
					
					// Create status update
					StatusUpdate upd2 = new StatusUpdate(p.getID(), UpdateType.EXT_AUTH);
					upd2.setDescription("Facebook Page Publishing Authorized");
					upd2.setAuthorID(p.getID());
					upds.add(upd2);
					
					// Set page token
					if (!SystemData.has("users.facebook.pageToken")) {
						SystemData.add("users.facebook.pageToken", appToken);
						FacebookCredentials fbCreds = (FacebookCredentials) SharedData.get(SharedData.FB_CREDS + SystemData.get("airline.code"));
						fbCreds.setPageToken(appToken);
					}
				}
			}
			
			// Start transaction
			ctx.startTX();
			
			// Write the updates
			SetPilot pwdao = new SetPilot(con);
			SetStatusUpdate sudao = new SetStatusUpdate(con);
			pwdao.write(p);
			sudao.write(upds);
			
			// Commit
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status variable
		ctx.setAttribute("fbAuth", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		result.setURL("/jsp/fbAuth.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}