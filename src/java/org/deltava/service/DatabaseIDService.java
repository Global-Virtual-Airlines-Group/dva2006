// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.sql.Connection;

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
	 * Executes the Web Service, writing ACARS flight data in KML format.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the name and password
		String name = ctx.getParameter("name");
		Pilot usr = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the user by name
			GetPilot pdao = new GetPilot(con);
			usr = pdao.getByName(name, SystemData.get("airline.db"));
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