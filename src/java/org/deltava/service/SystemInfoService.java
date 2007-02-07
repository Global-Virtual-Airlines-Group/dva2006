// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.io.*;
import java.util.Date;

import static javax.servlet.http.HttpServletResponse.*;

import org.apache.log4j.Logger;

import org.deltava.beans.fleet.SystemInformation;

import org.deltava.dao.SetLibrary;
import org.deltava.dao.DAOException;

/**
 * A Web Service to save System Data sent by a Fleet Installer.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SystemInfoService extends WebService {
	
	private static final Logger log = Logger.getLogger(SystemInfoService.class);

	/**
	 * Executes the Web Service, saving local system data.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Calculate the ID
		String id = ctx.getParameter("ID");
		if (id == null)
			id = Long.toHexString(System.currentTimeMillis());

		// Populate the SystemInformation bean from the request
		SystemInformation si = new SystemInformation(id);
		si.setDate(new Date());
		si.setCode(ctx.getParameter("AC"));
		si.setOS(ctx.getParameter("OS"));
		si.setDirectX(ctx.getParameter("DX"));
		si.setCPU(ctx.getParameter("CPU"));
		si.setGPU(ctx.getParameter("GPU"));

		// Parse memory size
		try {
			String memSize = ctx.getParameter("MEM");
			si.setRAM(Integer.parseInt(memSize.substring(0, memSize.indexOf("MB"))));
		} catch (Exception e) {
			si.setRAM(512);
		}

		// Parse Flight Simulator version
		try {
			String fsVersion = ctx.getParameter("VER");
			si.setFSVersion(Integer.parseInt(fsVersion.substring(2)));
		} catch (Exception e) {
			si.setFSVersion(2004);
		}

		// Write the system information to the database
		try {
			SetLibrary dao = new SetLibrary(ctx.getConnection());
			dao.write(si);
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
			throw new ServiceException(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Generate an INI-style response for the Fleet Installer
		ctx.println("[Installer]");
		ctx.print("ID=");
		ctx.println(si.getID());
		ctx.println("");
		
		try {
			ctx.getResponse().setContentType("text/plain");
		   ctx.commit();
		} catch (IOException ie) {
			throw new ServiceException(SC_CONFLICT, "I/O Error");
		}

		// Write result code
		return SC_OK;
	}
}