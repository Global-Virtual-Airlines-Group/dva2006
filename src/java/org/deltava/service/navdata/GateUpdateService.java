// Copyright 2015, 2017, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.navdata;

import java.util.*;
import java.sql.Connection;

import org.json.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.Simulator;
import org.deltava.beans.navdata.Gate;
import org.deltava.beans.navdata.GateZone;
import org.deltava.beans.schedule.Airport;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to update preferred Gate data. 
 * @author Luke
 * @version 10.0
 * @since 6.3
 */

public class GateUpdateService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Check access
		if (!ctx.isUserInRole("Schedule") && !ctx.isUserInRole("Operations"))
			return SC_FORBIDDEN;
		
		// Get the airport
		Airport a = SystemData.getAirport(ctx.getParameter("id"));
		if (a == null)
			return SC_NOT_FOUND;
		
		JSONArray ja = new JSONArray(new JSONTokener(ctx.getParameter("data")));
		Simulator sim = Simulator.fromName(ctx.getParameter("sim"), Simulator.FSX);
		try {
			Connection con = ctx.getConnection();
			GetGates gdao = new GetGates(con);
			Map<String, Gate> gm = CollectionUtils.createMap(gdao.getAllGates(a, sim), Gate::getName);
			
			// Update based on data
			Collection<Gate> updGates = new HashSet<Gate>(); 
			for (int x = 0; x < ja.length(); x++) {
				JSONObject go = ja.getJSONObject(x);
				JSONArray ga = go.optJSONArray("airlines");
				Gate g = gm.get(go.getString("id"));
				g.clearAirlines();
				for (int y = 0; (ga != null) && (y < ga.length()); y++)
					g.addAirline(SystemData.getAirline(ga.getString(y)));
				
				GateZone gz = GateZone.values()[go.optInt("zone", (go.optBoolean("intl") ? GateZone.INTERNATIONAL : GateZone.DOMESTIC).ordinal())];
				g.setZone(gz);
				updGates.add(g);
			}
			
			// Save gate data
			SetGates gwdao = new SetGates(con);
			gwdao.update(updGates);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
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