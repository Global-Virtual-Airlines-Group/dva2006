// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.simbrief;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.StringReader;
import java.sql.Connection;

import org.deltava.beans.flight.DraftFlightReport;
import org.deltava.beans.flight.HistoryType;
import org.deltava.beans.simbrief.*;

import org.deltava.dao.*;
import org.deltava.dao.http.*;
import org.deltava.dao.http.DAO.Compression;
import org.deltava.service.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Service to refresh SimBrief briefing packages. 
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class PackageRefreshService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		boolean isUpdated = false;
		try {
			Connection con = ctx.getConnection();
			
			// Load the Flight Report
			GetFlightReports frdao = new GetFlightReports(con);
			DraftFlightReport fr = frdao.getDraft(StringUtils.parse(ctx.getParameter("id"), 0), ctx.getDB());
			if (fr == null)
				throw error(SC_NOT_FOUND, "Invalid Flight Report - " + ctx.getParameter("id"), false);
			
			// Check our access
			PIREPAccessControl ac = new PIREPAccessControl(ctx, fr);
			ac.validate();
			if (!ac.getCanUseSimBrief())
				throw error(SC_FORBIDDEN, "Cannot refresh SimBrief package for Flight Report " + fr.getID(), false);
			
			// Load the briefing
			BriefingPackage pkg = frdao.getSimBrief(fr.getID(), ctx.getDB());
			if (pkg == null)
				throw error(SC_NOT_FOUND, "No SimBrief package for Flight Report " + fr.getID(), false);
			
			// Refresh the package
			GetSimBrief sbdao = new GetSimBrief();
			sbdao.setCompression(Compression.GZIP, Compression.BROTLI);
			sbdao.setConnectTimeout(3500);
			sbdao.setReadTimeout(4500);
			String data = sbdao.refresh(pkg.getSimBriefUserID(), fr.getHexID());
			
			// Parse the data
			BriefingPackage sbdata = SimBriefParser.parse(new StringReader(data));
			if (sbdata.getCreatedOn().isAfter(pkg.getCreatedOn())) {
				sbdata.setSimBriefID(pkg.getSimBriefID());
				sbdata.setURL(pkg.getURL());
				fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.DISPATCH, "Updated SimBrief package");
				isUpdated = true;
				
				// Update route if necessary
				if (!sbdata.getRoute().equals(fr.getRoute())) {
					fr.setRoute(sbdata.getRoute());
					fr.addStatusUpdate(ctx.getUser().getID(), HistoryType.DISPATCH, "Updated flight route via SimBrief");
				}
				
				// Write the data
				ctx.startTX();
				SetFlightReport frwdao = new SetFlightReport(con);
				frwdao.write(fr, ctx.getDB());
				frwdao.writeSimBrief(sbdata);
				ctx.commitTX();
			}
		} catch (Exception de) {
			ctx.rollbackTX();
			if (de instanceof HTTPDAOException) {
				HTTPDAOException hde = (HTTPDAOException) de;
				throw error (hde.getStatusCode(), hde.getMessage(), false);
			}

			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		return isUpdated ? SC_OK : SC_NOT_MODIFIED;
	}
}