// Copyright 2009, 2012, 2015, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import java.sql.Connection;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.cooler.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;

import org.deltava.util.*;

/**
 * A Web Service to allow Water Cooler quoting.
 * @author Luke
 * @version 7.3
 * @since 2.7
 */

public class CoolerQuoteService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the thread and post ID
		int threadID = StringUtils.parse(ctx.getParameter("id"), 0);
		int postID = StringUtils.parse(ctx.getParameter("post"), 0) - 1;
		
		JSONObject jo = new JSONObject();
		try {
			Connection con = ctx.getConnection();
			
			// Get the thread
			GetCoolerThreads tdao = new GetCoolerThreads(con);
			MessageThread mt = tdao.getThread(threadID, true);
			if (mt == null)
				throw error(SC_NOT_FOUND, "Unknown Thread ID - " + threadID, false);
			else if ((postID < 0) || (postID >= mt.getPosts().size()))
				throw error(SC_NOT_FOUND, "Unknown Post ID - " + postID, false);
			
			// Get the channel
			GetCoolerChannels cdao = new GetCoolerChannels(con);
			Channel ch = cdao.get(mt.getChannel());
			if (ch == null)
				throw error(SC_NOT_FOUND, "Unknown Channel - " + mt.getChannel(), false);
			
			// Validate our access to the thread
			CoolerThreadAccessControl ac = new CoolerThreadAccessControl(ctx);
			ac.updateContext(mt, ch);
			ac.validate();
			if (!ac.getCanReply())
				throw error(SC_UNAUTHORIZED, "Cannot reply to Thread", false);
			
			// Get the message
			Message msg = mt.getPosts().get(postID);
			
			// Load the User
			GetUserData uddao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			UserData ud = uddao.get(msg.getAuthorID());
			Pilot p = pdao.get(ud);
			
			// Return the data
			jo.put("body",  msg.getBody());
			if (p != null)
				jo.put("author", p.getName());
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("text/xml", "utf-8");
			ctx.println(jo.toString());
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
	
	/**
	 * Returns if the Web Service requires authentication.
	 * @return TRUE
	 */
	@Override
	public final boolean isSecure() {
		return true;
	}
}