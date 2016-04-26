// Copyright 2008, 2012, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.util.*;
import java.time.Instant;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.StringUtils;

/**
 * A Web Service to display aggregated ACARS chat logs.
 * @author Luke
 * @version 7.0
 * @since 2.2
 */

public class ChatLogService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Check our access
		if (!ctx.isUserInRole("HR"))
			throw error(SC_UNAUTHORIZED, "Not in HR Role", false);
		
		// Get the start/end dates
		Instant sd = StringUtils.parseInstant(ctx.getParameter("start"), "MM/dd/yyyy HH:mm");
		Instant ed = StringUtils.parseInstant(ctx.getParameter("end"), "MM/dd/yyyy HH:mm");
		
		// Build the criteria
		LogSearchCriteria criteria = new LogSearchCriteria(sd, ed);
		
		Collection<TextMessage> msgs = null;
		Map<Integer, Pilot> pilots = null;
		try {
			Connection con = ctx.getConnection();
			
			// Do the search
			GetACARSLog dao = new GetACARSLog(con);
			msgs = dao.getMessages(criteria, null);
			
			// Get the author/recipient IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			for (TextMessage msg : msgs) {
				IDs.add(Integer.valueOf(msg.getAuthorID()));
				IDs.add(Integer.valueOf(msg.getRecipientID()));
			}
			
			// Load the Users
			GetUserData uddao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			UserDataMap udm = uddao.get(IDs);
			pilots = pdao.get(udm);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Format the output
		for (Iterator<TextMessage> i = msgs.iterator(); i.hasNext(); ) {
			TextMessage msg = i.next();
			ctx.print(StringUtils.format(msg.getDate(), "MM/dd/yyyy HH:mm:ss"));
			ctx.print(",");
			Pilot author = pilots.get(Integer.valueOf(msg.getAuthorID()));
			ctx.print((author == null) ? "???" : author.getName());
			ctx.print(",");
			if (msg.getRecipientID() != 0) {
				Pilot recipient = pilots.get(Integer.valueOf(msg.getRecipientID()));
				ctx.print((recipient == null) ? "???" : recipient.getName());
			}
			
			ctx.print(",");
			ctx.println(msg.getMessage());
		}
		
		// Write the response
		try {
			ctx.setContentType("text/csv", "utf-8");
			ctx.setHeader("Content-disposition", "attachment; filename=acarsChatLog.csv");
			ctx.commit();
		} catch (Exception ie) {
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