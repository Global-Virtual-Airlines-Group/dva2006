// Copyright 2011, 2015, 2021, 2022, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.logbook;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.sql.Connection;
import java.io.IOException;
import java.lang.reflect.Constructor;

import org.deltava.beans.Pilot;
import org.deltava.beans.flight.*;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.StringUtils;

/**
 * A Web Service to export a Pilot's log book. 
 * @author Luke
 * @version 11.2
 * @since 3.6
 */

public class LogbookService extends WebService {
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the user ID
		Pilot p = ctx.getUser();
		int userID = p.getID();
		int id = StringUtils.parse(ctx.getParameter("id"), userID);
		if (ctx.isUserInRole("PIREP") || ctx.isUserInRole("HR"))
			userID = id;
		
		// Get the logbook exporter
		LogbookExport le = null;
		try {
			String cName = ctx.getParameter("export");
			if ((cName == null) || !Character.isLetter(cName.charAt(0)))
				return SC_BAD_REQUEST;
			
			Class<?> ec = Class.forName(String.format("org.deltava.service.logbook.%s", cName));
			Constructor<?> cc = ec.getConstructor((Class<?>[]) null);
			le = (LogbookExport) cc.newInstance((Object[]) null);
		} catch (Exception cnfe) {
			throw error(SC_BAD_REQUEST, cnfe.getMessage(), cnfe);
		}
		
		// Get the Flight Reports
		LogbookSearchCriteria lsc = new LogbookSearchCriteria("DATE, PR.SUBMITTED", ctx.getDB());
		lsc.setLoadComments(le instanceof JSONExport);
		Collection<FlightReport> pireps = null;
		try {
			Connection con = ctx.getConnection();
			
			// Load aircraft profiles
			GetAircraft acdao = new GetAircraft(con);
			le.loadAircraft(acdao.getAircraftTypes());
			
			// Load flights
			GetFlightReports frdao = new GetFlightReports(con);
			pireps = frdao.getByPilot(userID, lsc);
			frdao.loadCaptEQTypes(userID, pireps, ctx.getDB());
			
			// Load flight status history
			GetFlightReportHistory frhdao = new GetFlightReportHistory(con);
			frhdao.loadStatus(userID, pireps);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}

		// Format flights
		pireps.forEach(le::add);
		
		// Write the response
		String fileName = String.format("logbook_%d.%s", Integer.valueOf(userID), le.getExtension());
		try {
			ctx.setContentType(le.getContentType(), "utf-8");
			ctx.setHeader("Content-disposition", String.format("attachment; filename=%s", fileName));
			ctx.setHeader("X-Logbook-Filename", fileName);
			ctx.println(le.toString());
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}

	@Override
	public final boolean isSecure() {
		return true;
	}
}