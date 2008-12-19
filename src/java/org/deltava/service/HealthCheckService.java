// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Service to ensure the server JVM is still alive.
 * @author Luke
 * @version 2.3
 * @since 2.3
 */

public class HealthCheckService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Validate system data
		ctx.println(SystemData.get("airline.code") + " is OK");
		
		// Write the response
		try {
			ctx.getResponse().setContentType("text/plain");
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_INTERNAL_SERVER_ERROR, e.getMessage(), false);
		}
		
		return SC_OK;
	}
	
	/**
	 * Returns if the Web Service invocation is logged.
	 * @return FALSE
	 */
	public boolean isLogged() {
		return false;
	}
}