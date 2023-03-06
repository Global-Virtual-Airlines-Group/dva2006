// Copyright 2019, 2020, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.*;

import org.deltava.beans.acars.ArchiveHelper;
import org.deltava.service.*;

import org.deltava.util.StringUtils;

/**
 * A Web Service to serve serialized ACARS track data for analytics usage.
 * @author Luke
 * @version 10.5
 * @since 9.0
 */

public class RawTrackService extends DownloadService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Check role
		if (!ctx.isUserInRole("Developer"))
			return SC_FORBIDDEN;
		
		// Check for the archive
		int acarsID = StringUtils.parse(ctx.getParameter("id"), -1);
		File f = ArchiveHelper.getPositions(acarsID);
		if (!f.exists())
			return SC_NOT_FOUND;
		
		// Write the response
		try {
			ctx.setContentType("application/octet-stream");
			ctx.setHeader("Content-disposition", "attachment; filename=acars" + Integer.toHexString(acarsID).toUpperCase() + ".dat");
			sendFile(f, ctx.getResponse());
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}

	@Override
	public boolean isSecure() {
		return true;
	}
	
	@Override
	public boolean isLogged() {
		return false;
	}
}