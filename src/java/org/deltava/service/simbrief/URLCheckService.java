// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.simbrief;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.Simulator;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.Airline;
import org.deltava.beans.simbrief.*;

import org.deltava.dao.*;
import org.deltava.dao.http.*;
import org.deltava.dao.http.DAO.Compression;
import org.deltava.service.*;

import org.deltava.util.StringUtils;

/**
 * A Web Service to check for a generated SimBrief flight plan.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class URLCheckService extends WebService {
	
	private static final Logger log = Logger.getLogger(URLCheckService.class);
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get ID and variable
		String varName = ctx.getParameter("var");
		String id = ctx.getParameter("id");
		if (StringUtils.isEmpty(id))
			throw error(SC_BAD_REQUEST, "No flight plan ID", false);
		
		BriefingPackage sbdata = null;
		try {
			GetSimBrief sbdao = new GetSimBrief();
			sbdao.setCompression(Compression.GZIP, Compression.BROTLI);
			sbdao.setConnectTimeout(3500);
			sbdao.setReadTimeout(4500);
			sbdata = sbdao.load(id);
			
			// Load the Flight Report
			Connection con = ctx.getConnection();
			GetFlightReports frdao = new GetFlightReports(con);
			DraftFlightReport dfr = frdao.getDraft(sbdata.getID(), ctx.getDB());
			if (dfr == null)
				throw error(SC_NOT_FOUND, "Invalid Flight Report - " + sbdata.getID(), false);
			
			// Update route
			dfr.setAttribute(FlightReport.ATTR_SIMBRIEF, true);
			if (!sbdata.getRoute().equalsIgnoreCase(dfr.getRoute())) {
				dfr.setRoute(sbdata.getRoute());
				dfr.addStatusUpdate(ctx.getUser().getID(), HistoryType.DISPATCH, "Updated Route via SimBrief");
			}
			
			// Load gates if needed
			if (StringUtils.isEmpty(dfr.getGateD()) && StringUtils.isEmpty(dfr.getGateA())) {
				GateZone dgz = switch (dfr.getFlightType()) {
					case USPFI -> GateZone.USPFI;
					case INTERNATIONAL -> GateZone.INTERNATIONAL;
					default -> GateZone.DOMESTIC;
				};
				
				// Load departure gate
				GetGates gdao = new GetGates(con);
				List<Gate> dGates = filter(gdao.getPopularGates(dfr, Simulator.P3Dv4, true), dfr.getAirline(), dgz);
				if (!dGates.isEmpty())
					dfr.setGateD(dGates.get(0).getName());
				
				// Load arrival gate
				GateZone agz = (dfr.getFlightType() == FlightType.INTERNATIONAL) ? GateZone.INTERNATIONAL : GateZone.DOMESTIC;
				List<Gate> aGates = filter(gdao.getPopularGates(dfr, Simulator.P3Dv4, false), dfr.getAirline(), agz);
				if (!aGates.isEmpty())
					dfr.setGateA(aGates.get(0).getName());
			}
			
			// Start transaction
			ctx.startTX();
			
			// Persist the data
			SetFlightReport frwdao = new SetFlightReport(con);
			frwdao.write(dfr, ctx.getDB());
			frwdao.writeSimBrief(sbdata);
			ctx.commitTX();
		} catch (HTTPDAOException hde) {
			log.info(String.format("Response %d downloading %s", Integer.valueOf(hde.getStatusCode()), id));
			// not downloaded yet
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		try {
			ctx.setContentType("text/javascript", "utf-8");
			ctx.println(String.format("var %s = %s;", varName, Boolean.valueOf(sbdata != null)));
			if (sbdata != null)
				ctx.println(String.format("var apiFileName = '%s';", sbdata.getURL()));
			ctx.commit();
		} catch (IOException ie) {
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
	
	/*
	 * Helper method to filter gates.
	 */
	private static List<Gate> filter(Collection<Gate> gates, Airline a, GateZone gz) {
		List<Gate> fdGates = gates.stream().filter(g -> g.hasAirline(a)).collect(Collectors.toList());
		List<Gate> iGates = fdGates.stream().filter(g -> (g.getZone() == gz)).collect(Collectors.toList());
		return iGates.isEmpty() ? fdGates : iGates;
	}
}