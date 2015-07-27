// Copyright 2008, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Service to ensure the server JVM is still alive.
 * @author Luke
 * @version 6.1
 * @since 2.3
 */

public class HealthCheckService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		try {
			ctx.println(SystemData.get("airline.code") + " is OK");
			ctx.setContentType("text/plain", "utf-8");
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
	@Override
	public boolean isLogged() {
		return false;
	}
}