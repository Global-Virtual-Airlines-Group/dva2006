// Copyright 2007, 2008, 2016, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.hr;

import java.util.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.Pilot;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.StringUtils;

/**
 * A Web Service to return database IDs for ACARS users without Pilot codes.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class DatabaseIDService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the user by name
		String name = ctx.getParameter("name");
		String eMail = ctx.getParameter("eMail");
		Pilot usr = null;
		try {
			GetPilot pdao = new GetPilot(ctx.getConnection());
			List<Pilot> users = pdao.getByName(name, ctx.getDB());
			if (!StringUtils.isEmpty(eMail))
				users.removeIf(p -> !eMail.equalsIgnoreCase(p.getEmail()));
			
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
			ctx.setContentType("text/plain", "utf-8");
			ctx.setExpiry(30);
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
}