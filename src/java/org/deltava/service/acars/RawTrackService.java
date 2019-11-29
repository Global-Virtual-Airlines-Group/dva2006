// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.*;

import org.deltava.beans.acars.ArchiveHelper;
import org.deltava.service.*;

import org.deltava.util.StringUtils;

/**
 * A Web Service to serve serialized ACARS track data for analytics usage.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class RawTrackService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Check role
		if (ctx.isUserInRole("Developer"))
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
			try (OutputStream os = ctx.getResponse().getOutputStream()) {
				try (InputStream is = new FileInputStream(f)) {
					byte[] buffer = new byte[16384];
					int bytesRead = is.read(buffer);
					while (bytesRead != -1) {
						os.write(buffer, 0, bytesRead);
						bytesRead = is.read(buffer);
					}
				}
			}

			ctx.getResponse().setContentLengthLong(f.length());
			ctx.getResponse().flushBuffer();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
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