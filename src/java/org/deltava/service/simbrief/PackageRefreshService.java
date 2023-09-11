// Copyright 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.simbrief;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.List;
import java.sql.Connection;

import org.deltava.beans.Simulator;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.Gate;
import org.deltava.beans.schedule.GateHelper;
import org.deltava.beans.simbrief.*;

import org.deltava.dao.*;
import org.deltava.dao.http.*;
import org.deltava.service.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Service to refresh SimBrief briefing packages. 
 * @author Luke
 * @version 11.1
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
			GetSimBriefPackages sbpdao = new GetSimBriefPackages(con);
			BriefingPackage pkg = sbpdao.getSimBrief(fr.getID(), ctx.getDB());
			if (pkg == null)
				throw error(SC_NOT_FOUND, "No SimBrief package for Flight Report " + fr.getID(), false);
			
			// Refresh the package
			GetSimBrief sbdao = new GetSimBrief();
			sbdao.setCompression(Compression.GZIP, Compression.BROTLI);
			sbdao.setConnectTimeout(3500);
			sbdao.setReadTimeout(4500);
			sbdao.setReturnErrorStream(true);
			String data = sbdao.refresh(pkg.getSimBriefUserID(), fr.getHexID());
			
			// Parse the data
			BriefingPackage sbdata = SimBriefParser.parse(data);
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
			}
			
			// Get simulator if we can
			if (fr.getSimulator() == Simulator.UNKNOWN) {
				frdao.setQueryMax(10);
				List<FlightReport> pireps = frdao.getByPilot(ctx.getUser().getID(), new LogbookSearchCriteria("DATE DESC, PR.SUBMITTED DESC, PR.ID DESC", ctx.getDB()));
				LogbookHistoryHelper lh = new LogbookHistoryHelper(pireps);
				if (lh.isConsistentSimulator(3)) {
					fr.setSimulator(lh.getLastFlight().getSimulator());
					fr.addStatusUpdate(0, HistoryType.UPDATE, String.format("Updated Simulator to %s", fr.getSimulator()));
				}
			}
				
			// If we don't have gates, assign them
			if (!fr.hasGates() && (fr.getSimulator() != Simulator.UNKNOWN)) {
				GetGates gdao = new GetGates(con);
				GateHelper gh = new GateHelper(fr, 5, true);
				gh.addDepartureGates(gdao.getGates(fr.getAirportD()), gdao.getUsage(fr, true, ctx.getDB()));
				gh.addArrivalGates(gdao.getGates(fr.getAirportA()), gdao.getUsage(fr, false, ctx.getDB()));
					
				// Load departure gate
				List<Gate> dGates = gh.getDepartureGates();
				if (!dGates.isEmpty()) {
					fr.setGateD(dGates.get(0).getName());
					fr.addStatusUpdate(0, HistoryType.DISPATCH, String.format("Assigned Departure Gate %s", fr.getGateD()));
				}
					
				// Load arrival gate
				List<Gate> aGates = gh.getArrivalGates();
				if (!aGates.isEmpty()) {
					fr.setGateA(aGates.get(0).getName());
					fr.addStatusUpdate(0, HistoryType.DISPATCH, String.format("Assigned Arrival Gate %s", fr.getGateA()));	
				}
				
				isUpdated |= fr.hasGates();
			}
				
			// Write the data
			if (isUpdated) {
				ctx.startTX();
				SetFlightReport frwdao = new SetFlightReport(con);
				frwdao.write(fr, ctx.getDB());
				if (!StringUtils.isEmpty(sbdata.getSimBriefID())) frwdao.writeSimBrief(sbdata); // non-null if updated
				ctx.commitTX();
			}
		} catch (ServiceException se) {
			ctx.setHeader("X-SB-Error-Message", se.getMessage());
			throw se;
		} catch (Exception de) {
			ctx.rollbackTX();
			if (de instanceof HTTPDAOException hde) {
				String errorMsg = SimBriefParser.parseError(hde.getMessage());
				ctx.setHeader("X-SB-Error-Code", hde.getStatusCode());
				ctx.setHeader("X-SB-Error-Message", errorMsg);
				throw error (SC_SERVICE_UNAVAILABLE, errorMsg, false);
			}

			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		return isUpdated ? SC_OK : SC_NOT_MODIFIED;
	}
}