// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.IOException;
import java.sql.Connection;

import org.jdom.*;

import org.deltava.beans.*;
import org.deltava.beans.cooler.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerThreadAccessControl;

import org.deltava.util.*;

/**
 * A Web Service to allow Water Cooler quoting.
 * @author Luke
 * @version 2.7
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
		int postID = StringUtils.parse(ctx.getParameter("post"), 0);
		
		Document doc = new Document();
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
			Element re = new Element("msg");
			doc.setRootElement(re);
			re.addContent(XMLUtils.createElement("body", msg.getBody(), true));
			if (p != null)
				re.addContent(XMLUtils.createElement("author", p.getName(), true));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Dump the XML to the output stream
		try {
			ctx.getResponse().setContentType("text/xml");
			ctx.getResponse().setCharacterEncoding("UTF-8");
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		// Return success code
		return SC_OK;
	}
	
	/**
	 * Returns if the Web Service requires authentication.
	 * @return TRUE
	 */
	public final boolean isSecure() {
		return true;
	}
}