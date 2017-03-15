// Copyright 2008, 2009, 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.wx;

import java.sql.Connection;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

import org.deltava.beans.navdata.AirportLocation;
import org.deltava.beans.wx.METAR;

import org.deltava.dao.*;
import org.deltava.dao.wsdl.GetFAWeather;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to fetch METAR data. 
 * @author Luke
 * @version 7.3
 * @since 2.3
 */

public class METARService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Check if using FlightAware data services
		boolean useFA = Boolean.valueOf(ctx.getParameter("fa")).booleanValue();
		useFA &= SystemData.getBoolean("schedule.flightaware.enabled")
				&& (ctx.isUserInRole("Route") || ctx.isUserInRole("Dispatch"));
		
		// Get the weather data
		METAR data = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the geographic location
			String code = ctx.getParameter("code");
			if (useFA) {
				GetNavData navdao = new GetNavData(con);
				AirportLocation loc = navdao.getAirport(code);
				
				// Load the weather
				GetFAWeather wxdao = new GetFAWeather();
				wxdao.setUser(SystemData.get("schedule.flightaware.download.user"));
				wxdao.setPassword(SystemData.get("schedule.flightaware.download.pwd"));
				data = wxdao.getMETAR(loc);
			} else {
				GetWeather wxdao = new GetWeather(con);
				data = wxdao.getMETAR(code);
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Create the JSON document
		JSONObject jo = new JSONObject();
		jo.put("icao", data.getCode());
		jo.put("tabs", new JSONArray());
		jo.put("ll", GeoUtils.toJSON(data));
		jo.put("color", data.getIconColor());
		jo.put("type", data.getType().toString());
		jo.put("date", data.getDate().toEpochMilli() / 1000);
		jo.put("info", data.getInfoBox());
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.setExpiry(1800);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (Exception ex) {
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
}