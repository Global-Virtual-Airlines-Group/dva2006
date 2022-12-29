// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.servinfo;

import java.util.*;
import java.sql.Connection;
import java.util.stream.Collectors;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightReport;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to download Online Network Flight data for validation.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class OnlineFlightsService extends JSONDataService {

	/**
	 * Executes the Web Service, returning Pilot names and IDs.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the network
		OnlineNetwork net = EnumUtils.parse(OnlineNetwork.class, ctx.getParameter("network"), OnlineNetwork.valueOf(SystemData.get("online.default_network")));
		int max = Math.max(100, StringUtils.parse(ctx.getParameter("max"), 50));
		Collection<FlightReport> flights = new ArrayList<FlightReport>();
		Map<Integer, Pilot> pilots = new HashMap<Integer, Pilot>();
		try {
			Connection con = ctx.getConnection();

			// Get the Flight Reports
			GetFlightReports frdao = new GetFlightReports(con);
			frdao.setQueryMax(max);
			flights.addAll(frdao.getByNetwork(net, ctx.getDB()));

			// Load the Pilots
			GetPilot pdao = new GetPilot(con);
			Collection<Integer> IDs = flights.stream().map(FlightReport::getAuthorID).collect(Collectors.toSet());
			pilots.putAll(pdao.getByID(IDs, "PILOTS"));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}

		// If we're using JSON, init the document
		JSONObject jo = new JSONObject();  
		jo.put("network", net.toString());
		JSONObject jpo = new JSONObject(); jo.put("pilots", jpo);
		pilots.entrySet().stream().forEach(me -> jpo.put(me.getKey().toString(), format(me.getValue(), net)));
		
		// Write the flights
		for (FlightReport fr : flights) {
			Pilot p = pilots.get(Integer.valueOf(fr.getAuthorID()));
			if (p != null)
				jo.accumulate("flights", format(fr));
		}
		
		JSONUtils.ensureArrayPresent(jo, "flights");
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.setExpiry(3600);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
	
	/*
	 * Formats a Flight Report as a JSON object.
	 */
	private static JSONObject format(FlightReport fr) {
		
		JSONObject fo = new JSONObject();
		fo.put("id", fr.getID());
		fo.put("pilotID", fr.getAuthorID());
		fo.put("origin", JSONUtils.format(fr.getAirportD()));
		fo.put("destination", JSONUtils.format(fr.getAirportA()));
		fo.put("distance", fr.getDistance());
		fo.put("equipmentType", fr.getEquipmentType());
		fo.put("url", String.format("https://www.%s/pirep.do?id=%s", SystemData.get("airline.domain"), fr.getHexID()));
		fo.put("date", JSONUtils.formatDate(fr.getDate()));
		fo.put("submitted", JSONUtils.formatDate(fr.getSubmittedOn()));
		fo.putOpt("network", fr.getNetwork());
		return fo;
	}
}