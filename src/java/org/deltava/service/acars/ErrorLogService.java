// Copyright 2006, 2007, 2009, 2012, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.util.Base64;
import java.time.Instant;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.Simulator;
import org.deltava.beans.acars.*;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to log ACARS client errors.
 * @author Luke
 * @version 7.3
 * @since 1.0
 */

public class ErrorLogService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// If this isn't a post, just return a 400
		if (!ctx.getRequest().getMethod().equalsIgnoreCase("post"))
			return SC_BAD_REQUEST;
		
		// Create the error bean
		ACARSError err = new ACARSError(ctx.getUser().getID(), ctx.getParameter("msg"));
		err.setCreatedOn(Instant.now());
		err.setStackDump(ctx.getParameter("stackDump"));
		err.setClientBuild(StringUtils.parse(ctx.getParameter("clientBuild"), 1));
		err.setBeta(StringUtils.parse(ctx.getParameter("beta"), 0));
		err.setSimulator(Simulator.fromName(ctx.getParameter("fsVersion"), Simulator.UNKNOWN));
		err.setFSUIPCVersion(ctx.getParameter("fsuipcVersion"));
		err.setRemoteAddr(ctx.getRequest().getRemoteAddr());
		err.setRemoteHost(ctx.getRequest().getRemoteHost());
		err.setOSVersion(ctx.getParameter("os"));
		err.setIs64Bit(Boolean.valueOf(ctx.getParameter("is64")).booleanValue());
		err.setCLRVersion(ctx.getParameter("clr"));
		err.setLocale(ctx.getParameter("locale"));
		err.setTimeZone(ctx.getParameter("tz"));
		err.setStateData(ctx.getParameter("stateData"));
		err.setClientType(ClientType.PILOT);
		if (err.getClientBuild() < 75) {
			err.setClientType(ClientType.DISPATCH);
			err.setVersion(1);
		} else if (err.getClientBuild() < 80)
			err.setVersion(1);
		 else if (err.getClientBuild() < 100)
			 err.setVersion(2);
		 else
			 err.setVersion(3);
		
		// Parse the log if any
		String b64logData = ctx.getParameter("log");
		if (!StringUtils.isEmpty(b64logData))
			err.load(Base64.getDecoder().decode(b64logData));
		
		// Sanitfy check the message
		if (StringUtils.isEmpty(err.getMessage()))
			return SC_BAD_REQUEST;
		
		try {
			SetACARSLog dao = new SetACARSLog(ctx.getConnection());
			dao.logError(err);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}

		return SC_OK;
	}

	/**
	 * Tells the Web Service Servlet to secure this Service.
	 * @return TRUE
	 */
	@Override
	public final boolean isSecure() {
		return true;
	}
}