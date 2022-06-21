// Copyright 2008, 2009, 2012, 2016, 2017, 2020, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.wx;

import java.util.*;
import java.sql.Connection;

import org.json.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.MapEntry;
import org.deltava.beans.wx.*;
import org.deltava.beans.navdata.AirportLocation;
import org.deltava.beans.system.*;

import org.deltava.dao.*;
import org.deltava.dao.http.GetFAWeather;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to provide aggregated METAR/TAF data for an Airport.
 * @author Luke
 * @version 10.2
 * @since 2.3
 */

public class AirportWeatherService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Check if using FlightAware data services
		boolean useFA = Boolean.parseBoolean(ctx.getParameter("fa"));
		useFA &= SystemData.getBoolean("schedule.flightaware.enabled") && (ctx.isUserInRole("Route") || ctx.isUserInRole("Dispatch"));
		
		// Check the code
		String code = ctx.getParameter("code");
		if (code == null)
			return SC_BAD_REQUEST;

		// Get the weather types
		Collection<String> wxTypes = StringUtils.split(ctx.getParameter("type"), ",");
		boolean useMETAR = wxTypes.contains(WeatherDataBean.Type.METAR.toString());
		boolean useTAF = wxTypes.contains(WeatherDataBean.Type.TAF.toString());

		// Get the weather data
		Collection<WeatherDataBean> wxBeans = new ArrayList<WeatherDataBean>();
		AirportLocation al = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the airport
			GetNavData navdao = new GetNavData(con);
			al = navdao.getAirport(code);
			
			// Get the weather
			if (useFA) {
				GetFAWeather dao = new GetFAWeather();
				dao.setUser(SystemData.get("schedule.flightaware.flightXML.user"));
				dao.setPassword(SystemData.get("schedule.flightaware.flightXML.v3"));
				dao.setReadTimeout(5000);
				APILogger.add(new APIRequest(API.FlightAware.createName("WEATHER"), !ctx.isAuthenticated()));
				if (useMETAR)
					wxBeans.add(dao.getMETAR(al));
				if (useTAF)
					wxBeans.add(dao.getTAF(al));
			} else {
				GetWeather wxdao = new GetWeather(con);
				if (useMETAR)
					wxBeans.add(wxdao.getMETAR(code));
				if (useTAF)
					wxBeans.add(wxdao.getTAF(code));
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Create the XML document
		JSONObject jo = new JSONObject();
		jo.put("icao", code);

		// Add the METAR/TAF data
		if (al != null) {
			JSONObject wo = new JSONObject();
			wo.put("ll", JSONUtils.format(al));
			wo.put("color", MapEntry.WHITE);
			wo.put("icao", al.getCode());
			for (WeatherDataBean wx : wxBeans) {
				if (wx == null) continue;
				
				wx.setAirport(al);
				JSONObject to = new JSONObject();
				to.put("name", wx.getType().toString());
				to.put("type", wx.getType().toString());
				to.put("content", wx.getData());
				wo.append("tabs", to);
			}
			
			JSONUtils.ensureArrayPresent(wo, "tabs");
			jo.append("wx", wo);
		}

		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "wx");
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.setExpiry(1200);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (Exception ex) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}

	@Override
	public final boolean isSecure() {
		return true;
	}
}