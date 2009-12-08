// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.util.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.Pilot;

import org.deltava.dao.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to return database IDs for ACARS users without Pilot codes.
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public class DatabaseIDService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the user by name
		String name = ctx.getParameter("name");
		String eMail = ctx.getParameter("eMail");
		Pilot usr = null;
		try {
			GetPilot pdao = new GetPilot(ctx.getConnection());
			List<Pilot> users = pdao.getByName(name, SystemData.get("airline.db"));
			if (users.size() > 0) {
				for (Iterator<Pilot> i = users.iterator(); i.hasNext(); ) {
					Pilot p = i.next();
					if ((eMail != null) && (!eMail.equalsIgnoreCase(p.getEmail())))
						i.remove();
				}
			}
			
			// Get the first pilot
			usr = users.isEmpty() ? null : users.get(0);
		} catch (DAOException de ) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Validate that we have a user
		if (usr == null)
			return SC_NOT_FOUND;
		
		// Return an INI file with the name and ID
		ctx.println("[userInfo]");
		ctx.println("user=" + usr.getName());
		ctx.println("id=" + usr.getID());
		if (!StringUtils.isEmpty(usr.getPilotCode()))
			ctx.println("code=" + usr.getPilotCode());

		// Dump the text to the output stream
		try {
			ctx.getResponse().setContentType("text/plain");
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		// Return success code
		return SC_OK;
	}
}