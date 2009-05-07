// Copyright 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.wx;

import java.util.*;

import org.jdom.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.MapEntry;
import org.deltava.beans.wx.*;
import org.deltava.beans.navdata.AirportLocation;

import org.deltava.dao.*;

import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to provide aggregated METAR/TAF data for an Airport.
 * @author Luke
 * @version 2.6
 * @since 2.3
 */

public class AirportWeatherService extends WeatherDataService {

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
			if (useFA) {
				if (useMETAR)
					wxBeans.add(getFAData().getMETAR(code));
				if (useTAF)
					wxBeans.add(getFAData().getTAF(code));
			} else {
				if (useMETAR)
					wxBeans.add(getNOAAData(WeatherDataBean.Type.METAR, code));
				if (useTAF)
					wxBeans.add(getNOAAData(WeatherDataBean.Type.TAF, code));
			}

			// Get the geographic location
			GetNavData navdao = new GetNavData(ctx.getConnection());
			al = navdao.getAirport(code);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}

		// Create the XML document
		Document doc = new Document();
		Element re = new Element("weather");
		doc.setRootElement(re);

		// Add the METAR/TAF data
		if (al != null) {
			Element e = new Element("wx");
			re.addContent(e);
			e.setAttribute("lat", StringUtils.format(al.getLatitude(), "##0.00000"));
			e.setAttribute("lng", StringUtils.format(al.getLongitude(), "##0.00000"));
			e.setAttribute("color", MapEntry.WHITE);
			e.setAttribute("icao", al.getCode());
			e.setAttribute("tabs", String.valueOf(wxBeans.size()));
			for (WeatherDataBean wx : wxBeans) {
				wx.setAirport(al);
				Element te = new Element("tab");
				te.setAttribute("name", wx.getType());
				te.setAttribute("type", wx.getType());
				te.addContent(new CDATA(wx.getData()));
				e.addContent(te);
			}
		}

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