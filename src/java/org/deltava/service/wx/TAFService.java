// Copyright 2008, 2009, 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.wx;

import static javax.servlet.http.HttpServletResponse.*;

import java.sql.Connection;

import org.json.*;

import org.deltava.beans.wx.TAF;
import org.deltava.beans.navdata.AirportLocation;

import org.deltava.dao.*;
import org.deltava.dao.http.GetFAWeather;
import org.deltava.service.*;

import org.deltava.util.JSONUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to fetch Terminal Area Forecast data.
 * @author Luke
 * @version 8.0
 * @since 2.3
 */

public class TAFService extends WebService {

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
		useFA &= SystemData.getBoolean("schedule.flightaware.enabled") && (ctx.isUserInRole("Route") || ctx.isUserInRole("Dispatch"));

		// Get the weather data
		TAF data = null;
		try {
			Connection con = ctx.getConnection();
			
			String code = ctx.getParameter("code");
			if (useFA) {
				GetNavData navdao = new GetNavData(ctx.getConnection());
				AirportLocation al = navdao.getAirport(code);

				// Get the TAF
				GetFAWeather wxdao = new GetFAWeather();
				wxdao.setUser(SystemData.get("schedule.flightaware.flightXML.user"));
				wxdao.setPassword(SystemData.get("schedule.flightaware.flightXML.v3"));
				wxdao.setReadTimeout(5000);
				data = wxdao.getTAF(al);
			} else {
				GetWeather wxdao = new GetWeather(con);
				data = wxdao.getTAF(code);
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}

		// Create the JSON document
		JSONObject jo = new JSONObject();
		jo.put("icao", data.getCode());
		jo.put("ll", JSONUtils.format(data));
		jo.put("color", data.getIconColor());
		jo.put("type", data.getType().toString());
		jo.put("date", data.getDate().toEpochMilli() / 1000);
		jo.put("info", data.getInfoBox());
		JSONUtils.ensureArrayPresent(jo, "tabs");

		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "utf-8");
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