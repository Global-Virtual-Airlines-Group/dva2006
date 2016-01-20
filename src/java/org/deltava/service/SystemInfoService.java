// Copyright 2005, 2007, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

/**
 * A Web Service to save System Data sent by a Fleet Installer.
 * @author Luke
 * @version 6.4
 * @since 1.0
 */

public class SystemInfoService extends WebService {
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return 200 Always
	 */
	@Override
	public int execute(ServiceContext ctx) {
		return SC_OK;
	}
}