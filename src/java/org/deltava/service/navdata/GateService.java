// Copyright 2015, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.navdata;

import java.util.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.Airline;
import org.deltava.beans.schedule.Airport;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to return preferred airport Gate data. 
 * @author Luke
 * @version 7.3
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
		
		// Get the airport
		Airport a = SystemData.getAirport(ctx.getParameter("id"));
		if (a == null)
			return SC_NOT_FOUND;
		
		// Get simulator version
		Simulator sim = Simulator.fromName(ctx.getParameter("sim"), Simulator.FSX);
		Collection<Gate> gates = new LinkedHashSet<Gate>();
		try {
			GetGates gdao = new GetGates(ctx.getConnection());
			gates.addAll(gdao.getPopularGates(a, sim));
			gates.addAll(gdao.getAllGates(a, sim));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Create the JSON
		JSONObject jo = new JSONObject();
		jo.put("icao", a.getICAO());
		jo.put("ll", JSONUtils.format(a));
		
		// Write gates
		for (Gate g : gates) {
			JSONObject go = new JSONObject();
			go.put("id", g.getName());
			go.put("ll", JSONUtils.format(g));
			go.put("isIntl", g.isInternational());
			go.put("useCount", g.getUseCount());
			go.put("info", g.getInfoBox());
			go.put("airlines", new JSONArray());
			for (Airline al : g.getAirlines())
				go.accumulate("airlines", al.getCode());
			
			jo.accumulate("gates", go);
		}
		
		// Write the JSON document
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.setExpiry(60);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
	
	/**
	 * Returns whether this web service requires authentication.
	 * @return TRUE always
	 */
	@Override
	public final boolean isSecure() {
		return true;
	}

	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE always
	 */
	@Override
	public final boolean isLogged() {
		return false;
	}
}