// Copyright 2005, 2007, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.util.system.SystemData;

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
	public int execute(ServiceContext ctx) throws ServiceException {
		
		ctx.println("[sites]");
		ctx.println("options=DVA Main Server");
		ctx.println("");
		ctx.print("[");
		ctx.print(SystemData.get("airline.code"));
		ctx.println(" Main Server]");
		ctx.println("host=" + SystemData.get("airline.url"));
		ctx.println("path=/install");

		ctx.println("[currentMirror]");
		ctx.println("host=" + SystemData.get("airline.url"));
		ctx.println("path=/install");
		
		try {
			ctx.setContentType("text/plain", "utf-8");
			ctx.setExpiry(3600);
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
}