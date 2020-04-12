// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.sql.Connection;
import java.io.IOException;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.event.*;

import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Service to display JSON-formatted event data. 
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class EventInfoService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the start date or event ID
		int eventID = StringUtils.parse(ctx.getParameter("id"), -1);
		Instant dt = StringUtils.parseEpoch(ctx.getParameter("startDate"));
		int days = Math.min(180, StringUtils.parse(ctx.getParameter("days"), 30));
		
		Collection<Event> results = new ArrayList<Event>();
		Map<Integer, Pilot> pilots = new HashMap<Integer, Pilot>();
		try {
			Connection con = ctx.getConnection();
			
			// Load the events and pilot IDs
			GetEvent dao = new GetEvent(con);
			if (eventID > 0) {
				Event e = dao.get(eventID);
				if (e != null)
					results.add(e);
			} else
				results.addAll(dao.getEventCalendar(new DateRange(dt, dt.plus(days, ChronoUnit.DAYS))));

			// Find IDs
			Collection<Integer> IDs = results.stream().map(e -> e.getSignups()).flatMap(Collection::stream).map(Signup::getPilotID).collect(Collectors.toSet());
			
			// Load pilots
			GetPilot pdao = new GetPilot(con);
			GetUserData uddao = new GetUserData(con);
			UserDataMap udmap = uddao.get(IDs);
			for (String tableName : udmap.getTableNames())
				pilots.putAll(pdao.getByID(udmap.getByTable(tableName), tableName));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Convert to JSON
		JSONArray ja = new JSONArray();
		for (Event e : results) {
			JSONObject eo = new JSONObject();
			eo.put("name", e.getName());
			eo.put("id", e.getID());
			eo.put("network", e.getNetwork().name());
			eo.put("startDateTime", e.getStartTime().toEpochMilli());
			eo.put("endDateTime", e.getEndTime().toEpochMilli());
			eo.put("signupDeadline", e.getSignupDeadline().toEpochMilli());
			eo.put("signupPermitted", e.getCanSignup());
			eo.putOpt("signupURL", e.getSignupURL());
			
			// Add routes
			for (Route r : e.getRoutes()) {
				JSONObject ro = new JSONObject();
				ro.put("id", r.getRouteID());
				ro.put("name", r.getName());
				ro.put("route", r.getRoute());
				ro.put("airportD", r.getAirportD().getIATA());
				ro.put("airportA", r.getAirportA().getIATA());
				ro.put("active", r.getActive());
				ro.put("distance", r.getDistance());
				ro.put("isRNAV", r.getIsRNAV());
				ro.put("signups", r.getSignups());
				ro.put("maxSignups", r.getMaxSignups());
				eo.append("routes", ro);
			}
			
			// Add signups
			for (Signup s : e.getSignups()) {
				Pilot p = pilots.get(Integer.valueOf(s.getPilotID()));
				JSONObject so = new JSONObject();
				so.put("pilotID", s.getPilotID());
				so.put("routeID", s.getRouteID());
				if (p != null) {
					so.put("name", p.getName());
					so.put("pilotCode", p.getPilotCode());
					so.put("networkID", p.getNetworkID(e.getNetwork()));
				}
				
				eo.append("signups", so);
			}
			
			eo.put("owner", e.getOwner().getCode());
			e.getAirlines().forEach(ai -> eo.append("appCodes", ai.getCode()));
			JSONUtils.ensureArrayPresent(eo, "routes", "signups", "appCodes");
			ja.put(eo);
		}
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.setExpiry(600);
			ctx.println(ja.toString());
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}
}