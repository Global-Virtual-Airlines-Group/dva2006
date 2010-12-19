// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.security;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.fb.ProfileInfo;

import org.deltava.commands.*;

import org.deltava.dao.*;
import org.deltava.dao.http.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to handle Facebook authorization commands.
 * @author Luke
 * @version 3.4
 * @since 3.4
 */

public class FacebookAuthCommand extends AbstractCommand {

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
			// Load the user
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
			if (ctx.isUserInRole("Admin"))
				ctx.setAttribute("fbToken", token, REQUEST);
			
			// Get the user's information
			GetFacebookData fbdao = new GetFacebookData();
			fbdao.setToken(token);
			ProfileInfo info = fbdao.getUserInfo();
			
			// Update the user
			p.setIMHandle(IMAddress.FB, info.getID());
			p.setIMHandle(IMAddress.FBTOKEN, token);
			
			// Create status update
			StatusUpdate upd = new StatusUpdate(p.getID(), StatusUpdate.EXT_AUTH);
			upd.setDescription("Facebook Authorized");
			upd.setAuthorID(p.getID());
			
			// Start transaction
			ctx.startTX();
			
			// Write the updates
			SetPilot pwdao = new SetPilot(con);
			SetStatusUpdate sudao = new SetStatusUpdate(con);
			pwdao.write(p);
			sudao.write(upd);
			
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