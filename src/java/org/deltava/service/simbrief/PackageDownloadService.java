// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.simbrief;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.simbrief.BriefingPackage;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Service to download a SimBrief briefing package.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class PackageDownloadService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		BriefingPackage sbdata = null;
		try {
			GetFlightReports frdao = new GetFlightReports(ctx.getConnection());
			FlightReport fr = frdao.get(StringUtils.parse(ctx.getParameter("id"), 0), ctx.getDB());
			if (fr == null)
				throw error(SC_NOT_FOUND, "Invalid Flight Report - " + ctx.getParameter("id"), false);
			
			// Check our access
			PIREPAccessControl ac = new PIREPAccessControl(ctx, fr);
			ac.validate();
			if (!ac.getCanUseSimBrief())
				throw error(SC_FORBIDDEN, "Cannot view SimBrief package for Flight " + fr.getID(), false);
			
			// Load the package
			sbdata = frdao.getSimBrief(fr.getID(), ctx.getDB());
			if (sbdata == null)
				throw error(SC_NOT_FOUND, "No SimBrief package for Flight " + fr.getID(), false);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		try {
			ctx.setContentType("text/xml", "utf-8");
			ctx.println(sbdata.getXML());
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}

	@Override
	public final boolean isLogged() {
		return false;
	}
	
	@Override
	public final boolean isSecure() {
		return true;
	}
}