// Copyright 2005, 2007, 2015, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.io.*;
import java.util.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.fleet.Installer;

import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Service to display Fleet Library Information.
 * @author Luke
 * @version 8.4
 * @since 1.0
 */

public class FleetInfoService extends WebService {

	/**
	 * Executes the Web Service, returning an INI file for use with the Fleet Installers.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		Collection<Installer> entries = null;
		try {
			GetLibrary dao = new GetLibrary(ctx.getConnection());
			entries = dao.getFleet(SystemData.get("airline.db"), false);
		} catch (DAOException de) {
			throw new ServiceException(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		// Build the response
		ctx.println("[sites]");
		
		// Write the (legacy) mirror info
		ctx.print("options=");
		ctx.println(SystemData.get("airline.name"));
		ctx.println("\n[" + SystemData.get("airline.name") + "]");
		ctx.print("host=dl.");
		ctx.println(SystemData.get("airline.domain"));
		ctx.println("path=/install");
		ctx.println("\n[currentMirror]");
		ctx.print("host=dl.");
		ctx.println(SystemData.get("airline.domain"));
		ctx.println("path=/install");
		
		// Write installer version info
		ctx.println("\n[versionInfo]");
		for (Installer fe : entries) {
			ctx.print(fe.getCode());
			ctx.print("=");
			ctx.print(String.valueOf(fe.getMajorVersion()));
			ctx.print(String.valueOf(fe.getMinorVersion()));
			ctx.println(String.valueOf(fe.getSubVersion()));
		}

		try {
		   ctx.setContentType("text/plain", "utf-8");
		   ctx.commit();
		} catch (IOException ie) {
			throw new ServiceException(SC_CONFLICT, "I/O Error");
		}

		return SC_OK;
	}
}