// Copyright 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.simbrief;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.json.*;

import org.deltava.beans.flight.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to display SimBrief custom airframe data.
 * @author Luke
 * @version 11.2
 * @since 10.4
 */

public class CustomAirframeService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		Collection<Airframe> results = new ArrayList<Airframe>();
		Collection<Airframe> frResults = new LinkedHashSet<Airframe>();
		try {
			Connection con = ctx.getConnection();
			
			// Get the flight report
			GetFlightReportACARS frdao = new GetFlightReportACARS(con);
			FlightReport fr = frdao.get(StringUtils.parse(ctx.getParameter("id"), 0), ctx.getDB());
			if (fr == null)
				throw error(SC_NOT_FOUND, "Invalid Flight Report - " + ctx.getParameter("id"), false);
			
			// Check our access
			PIREPAccessControl ac = new PIREPAccessControl(ctx, fr);
			ac.validate();
			if (!ac.getCanUseSimBrief())
				throw error(SC_FORBIDDEN, "Cannot generate SimBrief package for Flight Report " + fr.getID(), false);
			
			// Load the airframes / tail codes
			GetSimBriefPackages sbdao = new GetSimBriefPackages(con);
			results.addAll(sbdao.getAirframes(fr.getEquipmentType(), fr.getAirline(), fr.getAuthorID()));
			frResults.addAll(frdao.getAirframes(fr.getEquipmentType(), fr.getAirline(), fr.getAuthorID()));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Remove SimBrief air frames without tail code from a PIREP
		Collection<String> tailCodes = frResults.stream().map(Airframe::getTailCode).collect(Collectors.toSet());
		results.removeIf(a -> !tailCodes.contains(a.getTailCode()));
		
		// Create the JSON object for airframes with SB data
		JSONArray ja = new JSONArray();
		for (Airframe a : results) {
			JSONObject ao = new JSONObject();
			ao.put("tailCode", a.getTailCode());
			ao.put("sdk", a.getSDK());
			ao.put("useCount", a.getUseCount());
			ao.put("lastUsed", JSONUtils.formatDate(a.getLastUse()));
			if (!StringUtils.isEmpty(a.getSimBriefID())) {
				ao.put("isCustom", true);
				ao.put("id", a.getSimBriefID());
			}
			
			ja.put(ao);
			frResults.removeIf(af -> af.getTailCode().equals(a.getTailCode())); // slow but these are small lists
		}
		
		// Add tail codes
		for (Airframe a : frResults) {
			JSONObject ao = new JSONObject();
			ao.put("tailCode", a.getTailCode());
			ao.put("sdk", a.getSDK());
			ao.put("useCount", a.getUseCount());
			ao.put("lastUsed", JSONUtils.formatDate(a.getLastUse()));
			ja.put(ao);
		}
		
		// Format the response
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.println(ja.toString());
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