// Copyright 2011, 2012, 2013, 2015, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.File;

import org.deltava.service.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Service to download the ACARS incremental installer.
 * @author Luke
 * @version 10.2
 * @since 4.1
 */

public class UpdateDownloadService extends DownloadService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the installer to download
		boolean isDispatch = Boolean.parseBoolean(ctx.getParameter("dispatch"));
		boolean isBeta = Boolean.parseBoolean(ctx.getParameter("beta"));
		StringBuilder buf = new StringBuilder(SystemData.get("airline.code"));
		buf.append(isDispatch ? "-Dispatch" : "-ACARS3");
		if (isBeta)
			buf.append("Beta");
		
		buf.append("Inc.exe");

		// Get the installer
		File f = new File(SystemData.get("path.library"), buf.toString());
		if (!f.exists() && !isBeta)
			return SC_NOT_FOUND;
		else if (!f.exists()) {
			f = new File(f.getParent(), f.getName().replace("Beta", ""));
			if (!f.exists())
				return SC_NOT_FOUND;
		}
		
		// Download the file
		ctx.setHeader("Content-disposition", "attachment; filename=" + buf.toString());
		ctx.setContentType("application/octet-stream");
		ctx.setExpiry(3600);
		sendFile(f, ctx.getResponse());
		return SC_OK;
	}

	/**
	 * Returns whether this web service requires authentication.
	 * @return TRUE always
	 */
	@Override
	public boolean isSecure() {
		return true;
	}
}