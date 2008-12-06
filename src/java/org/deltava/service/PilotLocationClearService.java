// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import java.sql.Connection;

import org.deltava.beans.GeoLocation;

import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to clear invalid Pilot Locations from the map.
 * @author Luke
 * @version 2.3
 * @since 2.3
 */

public class PilotLocationClearService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		if (!ctx.isUserInRole("HR"))
			throw error(SC_UNAUTHORIZED, "Not in HR role", false);

		GeoLocation loc = null;
		try {
			Connection con = ctx.getConnection();
			int id = StringUtils.parseHex(ctx.getParameter("id"));

			// Get the pilot
			GetPilot pdao = new GetPilot(con);
			loc = pdao.getLocation(id);

			// Delete the entry
			if (loc != null) {
				SetPilot pwdao = new SetPilot(con);
				pwdao.clearLocation(id);
			}
		} catch (NumberFormatException nfe) {
			throw error(SC_BAD_REQUEST, ctx.getParameter("id"), false);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Return success code
		return (loc == null) ? SC_NOT_FOUND : SC_OK;
	}

	/**
	 * Returns if the Web Service invocation is logged.
	 * @return FALSE
	 */
	public boolean isLogged() {
		return false;
	}

	/**
	 * Returns whether this web service requires authentication.
	 * @return TRUE if authentication is required, otherwise FALSE
	 */
	public boolean isSecure() {
		return true;
	}
}