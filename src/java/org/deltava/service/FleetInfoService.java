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
			entries = dao.getFleet();
		} catch (DAOException de) {
			throw new ServiceException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, de.getMessage());
		}
		
		// Build the response
		StringBuffer buf = new StringBuffer("[sites]\n");
		
		// Write the (legacy) mirror info
		buf.append("options=");
		buf.append(SystemData.get("airline.name"));
		buf.append("\n\n");
		buf.append("[" + SystemData.get("airline.name") + "]");
		buf.append("host=");
		buf.append(ctx.getRequest().getServerName());
		buf.append("\n");
		buf.append("path=/install\n");
		buf.append("\n[currentMirror]\n");
		buf.append("host=\n");
		buf.append("path=\n");
		
		// Write installer version info
		buf.append("\n[versionInfo]\n");
		for (Iterator i = entries.iterator(); i.hasNext();) {
			Installer fe = (Installer) i.next();
			if (fe.getCode() != null) {
				buf.append(fe.getCode());
				buf.append('=');
				buf.append(String.valueOf(fe.getMajorVersion()));
				buf.append(String.valueOf(fe.getMinorVersion()));
				buf.append(String.valueOf(fe.getSubVersion()));
				buf.append("\n");
			}
		}

		// Set the content type and length, and flush
		ctx.getResponse().setContentType("text/plain");
		ctx.getResponse().setContentLength(buf.length());
		ctx.getResponse().setBufferSize(buf.length() + 2);
	
		try {
			ctx.getResponse().getWriter().print(buf.toString());
			ctx.getResponse().flushBuffer();
		} catch (IOException ie) {
			throw new ServiceException(HttpServletResponse.SC_CONFLICT, "I/O Error");
		}

		// Return success code
		return HttpServletResponse.SC_OK;
	}
}