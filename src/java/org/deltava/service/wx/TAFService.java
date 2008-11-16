// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.wx;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;

import org.deltava.beans.wx.WeatherDataBean;
import org.deltava.beans.navdata.AirportLocation;

import org.deltava.dao.DAOException;
import org.deltava.dao.GetNavData;

import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to fetch Terminal Area Forecast data.
 * @author Luke
 * @version 2.3
 * @since 2.3
 */

public class TAFService extends WeatherDataService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Check if using FlightAware data services
		boolean useFA = Boolean.valueOf(ctx.getParameter("fa")).booleanValue();
		useFA &= SystemData.getBoolean("schedule.flightaware.enabled")
				&& (ctx.isUserInRole("Route") || ctx.isUserInRole("Dispatch"));

		// Get the weather data
		WeatherDataBean data = null;
		try {
			String code = ctx.getParameter("code");
			if (useFA)
				data = getFAData().getTAF(code);
			else
				data = getNOAAData("TAF", code);
			
			// Get the geographic location
			GetNavData navdao = new GetNavData(ctx.getConnection());
			AirportLocation al = navdao.getAirport(code);
			data.setAirport(al);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}

		// Create the XML document
		Document doc = new Document();
		Element re = new Element("weather");
		doc.setRootElement(re);

		// Add the TAF data
		Element e = XMLUtils.createElement("wx", data.getInfoBox(), true);
		e.setAttribute("tabs", "0");
		e.setAttribute("lat", StringUtils.format(data.getLatitude(), "##0.00000"));
		e.setAttribute("lng", StringUtils.format(data.getLongitude(), "##0.00000"));
		e.setAttribute("color", data.getIconColor());
		e.setAttribute("type", data.getType());
		e.setAttribute("icao", data.getCode());
		e.setAttribute("date", String.valueOf(data.getDate().getTime() / 1000));
		re.addContent(e);

		// Dump the XML to the output stream
		try {
			ctx.getResponse().setContentType("text/xml");
			ctx.getResponse().setCharacterEncoding("UTF-8");
			ctx.println(XMLUtils.format(doc, "UTF-8"));
			ctx.commit();
		} catch (Exception ex) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		// Return success code
		return SC_OK;
	}

	/**
	 * Returns whether this web service requires authentication.
	 * @return TRUE always
	 */
	public final boolean isSecure() {
		return true;
	}
}