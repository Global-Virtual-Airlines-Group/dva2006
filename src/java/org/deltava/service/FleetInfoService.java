// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

import java.io.*;
import java.util.*;
import javax.servlet.http.HttpServletResponse;

import org.deltava.beans.fleet.Installer;

import org.deltava.dao.GetLibrary;
import org.deltava.dao.DAOException;

import org.deltava.util.system.SystemData;

/**
 * A Web Service to display Fleet Library Information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FleetInfoService extends WebDataService {

	/**
	 * Executes the Web Service, returning an INI file for use with the Fleet Installers.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		List entries = null;
		try {
			GetLibrary dao = new GetLibrary(_con);
			entries = dao.getFleet(SystemData.get("airline.db"));
		} catch (DAOException de) {
			throw new ServiceException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, de.getMessage());
		}
		
		// Build the response
		ctx.println("[sites]");
		
		// Write the (legacy) mirror info
		ctx.print("options=");
		ctx.println(SystemData.get("airline.name"));
		ctx.println("\n[" + SystemData.get("airline.name") + "]");
		ctx.print("host=");
		ctx.println(ctx.getRequest().getServerName());
		ctx.println("path=/install");
		ctx.println("\n[currentMirror]");
		ctx.println("host=");
		ctx.println("path=");
		
		// Write installer version info
		ctx.println("\n[versionInfo]");
		for (Iterator i = entries.iterator(); i.hasNext();) {
			Installer fe = (Installer) i.next();
			if (fe.getCode() != null) {
				ctx.print(fe.getCode());
				ctx.print("=");
				ctx.print(String.valueOf(fe.getMajorVersion()));
				ctx.print(String.valueOf(fe.getMinorVersion()));
				ctx.println(String.valueOf(fe.getSubVersion()));
			}
		}

		// Set the content type and length, and flush
		try {
		   ctx.getResponse().setContentType("text/plain");
		   ctx.commit();
		} catch (IOException ie) {
			throw new ServiceException(HttpServletResponse.SC_CONFLICT, "I/O Error");
		}

		// Return success code
		return HttpServletResponse.SC_OK;
	}
}