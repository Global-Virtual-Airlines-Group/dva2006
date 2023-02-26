// Copyright 2015, 2017, 2019, 2021, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.navdata;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.comparators.AirlineComparator;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to return preferred airport Gate data. 
 * @author Luke
 * @version 10.5
 * @since 6.3
 */

public class GateService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the airport(s)
		Airport a = SystemData.getAirport(ctx.getParameter("id"));
		if (a == null)
			return SC_NOT_FOUND;
		
		Collection<Gate> gates = new LinkedHashSet<Gate>(); Collection<Airline> airlines = new TreeSet<Airline>(new AirlineComparator(AirlineComparator.NAME)); 
		try {
			Connection con = ctx.getConnection();
			
			// Load Gates
			GetGates gdao = new GetGates(con);
			gates.addAll(gdao.getGates(a));
			
			// Load airlines
			GetRawSchedule rsdao = new GetRawSchedule(con);
			airlines.addAll(rsdao.getAirlines(null, a).stream().filter(al -> !al.getHistoric()).collect(Collectors.toList()));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Create the JSON
		JSONObject jo = new JSONObject();
		jo.put("icao", a.getICAO());
		jo.put("airportD", JSONUtils.format(a));
		jo.put("maxUse", gates.stream().mapToInt(Gate::getUseCount).max().orElse(0));
		
		// Write airlines
		for (Airline al : airlines) {
			JSONObject ao = new JSONObject();
			ao.put("code", al.getCode());
			ao.put("name", al.getName());
			ao.put("historic", al.getHistoric());
			ao.put("color", al.getColor());
			jo.accumulate("airlines", ao);
		}
		
		// Write gate zones
		List<GateZone> zones = new ArrayList<GateZone>(Arrays.asList(GateZone.values()));
		if (!a.getHasPFI()) zones.remove(GateZone.USPFI);
		for (GateZone gz : zones) {
			JSONObject zo = new JSONObject();
			zo.put("id", gz.name());
			zo.put("description", gz.getDescription());
			jo.accumulate("zones", zo);
		}
		
		// Write gates
		for (Gate g : gates) {
			SelectableGate sg = new SelectableGate(g);
			sg.setZoneOptions(zones);
			sg.setAirlineOptions(airlines);
			JSONObject go = new JSONObject();
			go.put("id", sg.getUniqueID());
			go.put("name", sg.getName());
			go.put("info", sg.getInfoBox());
			go.put("ll", JSONUtils.format(sg));
			go.put("zone", sg.getZone().ordinal());
			go.put("useCount", sg.getUseCount());
			sg.getAirlines().stream().filter(al -> !al.getHistoric()).forEach(al -> go.accumulate("airlines", al.getCode()));
			for (Map.Entry<String, String> me : sg.getTabs().entrySet()) {
				JSONObject to = new JSONObject();
				to.put("name", me.getKey());
				to.put("content", me.getValue());
				go.accumulate("tabs", to);
			}
			
			JSONUtils.ensureArrayPresent(go, "airlines", "tabs");
			jo.accumulate("gates", go);
		}
		
		// Write the JSON document
		JSONUtils.ensureArrayPresent(jo, "gates", "zones", "airlines");
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.setExpiry(20);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
	
	@Override
	public final boolean isSecure() {
		return true;
	}

	@Override
	public final boolean isLogged() {
		return false;
	}
}