// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.util.Date;

import static javax.servlet.http.HttpServletResponse.*;

import org.apache.log4j.Logger;

import org.deltava.beans.acars.ACARSError;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to log ACARS client errors.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ErrorLogService extends WebDataService {

	private static final Logger log = Logger.getLogger(ErrorLogService.class);

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// If this isn't a post, just return a 200
		if (!ctx.getRequest().getMethod().equalsIgnoreCase("post"))
			return SC_OK;
		
		// Create the error bean
		ACARSError err = new ACARSError(ctx.getUser().getID(), ctx.getParameter("msg"));
		err.setCreatedOn(new Date());
		err.setStackDump(ctx.getParameter("stackDump"));
		err.setClientBuild(StringUtils.parse(ctx.getParameter("clientBuild"), 1));
		err.setFSVersion(StringUtils.parse(ctx.getParameter("fsVersion"), 2004));
		err.setFSUIPCVersion(ctx.getParameter("fsuipcVersion"));
		err.setRemoteAddr(ctx.getRequest().getRemoteAddr());
		err.setRemoteHost(ctx.getRequest().getRemoteHost());
		err.setStateData(ctx.getParameter("stateData"));
		
		try {
			SetACARSLog dao = new SetACARSLog(_con);
			dao.logError(err);
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
			return SC_INTERNAL_SERVER_ERROR;
		}

		// Return status code
		return SC_OK;
	}

	/**
	 * Tells the Web Service Servlet to secure this Service.
	 * @return TRUE
	 */
	public final boolean isSecure() {
		return true;
	}
}