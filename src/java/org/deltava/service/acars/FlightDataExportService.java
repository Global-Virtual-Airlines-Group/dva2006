// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.util.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.acars.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.StringUtils;

/**
 * A Web Service to return ACARS flight data parameters.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FlightDataExportService extends WebDataService {

	/**
	 * Executes the Web Service, writing ACARS Flight data in CSV format.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the ACARS Flight ID
		int id = StringUtils.parse(ctx.getParameter("id"), 0);
		if (id < 1)
			throw error(SC_BAD_REQUEST, "Invalid ID");

		// Get the ACARS data
		List<RouteEntry> routeData = null; 
		try {
			GetACARSData dao = new GetACARSData(_con);
			FlightInfo info = dao.getInfo(id);
			if (info != null) 
				routeData = dao.getRouteEntries(id, info.getArchived());
			else 
				routeData = Collections.emptyList();
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		}

		// Write the CSV header
		ctx.print("Date/Time,Latitude,Longitude,Altitude,Heading,Air Speed,Ground Speed,Vertical Speed,N1,N2,Bank,Pitch,Flaps,");
		ctx.println("WindSpeed,WindHdg,FuelFlow,Gs,AOA,NAV,HDG,APR,ALT,AT");

		// Format the ACARS data
		for (Iterator i = routeData.iterator(); i.hasNext();) {
			RouteEntry entry = (RouteEntry) i.next();
			ctx.print(StringUtils.format(entry.getDate(), "MM/dd/yyyy HH:mm:ss"));
			ctx.print(",");
			ctx.print(StringUtils.format(entry.getLatitude(), "##0.0000"));
			ctx.print(",");
			ctx.print(StringUtils.format(entry.getLongitude(), "##0.0000"));
			ctx.print(",");
			ctx.print(String.valueOf(entry.getAltitude()));
			ctx.print(",");
			ctx.print(StringUtils.format(entry.getHeading(), "000"));
			ctx.print(",");
			ctx.print(StringUtils.format(entry.getAirSpeed(), "##0"));
			ctx.print(",");
			ctx.print(StringUtils.format(entry.getGroundSpeed(), "##0"));
			ctx.print(",");
			ctx.print(StringUtils.format(entry.getVerticalSpeed(), "##0"));
			ctx.print(",");
			ctx.print(StringUtils.format(entry.getN1(), "##0.0"));
			ctx.print(",");
			ctx.print(StringUtils.format(entry.getN2(), "##0.0"));
			ctx.print(",");
			ctx.print(StringUtils.format(entry.getBank(), "##0.0"));
			ctx.print(",");
			ctx.print(StringUtils.format(entry.getPitch(), "##0.0"));
			ctx.print(",");
			ctx.print((entry.getFlaps() == 0) ? "" : String.valueOf(entry.getFlaps()));
			ctx.print(",");
			ctx.print(StringUtils.format(entry.getWindSpeed(), "##0"));
			ctx.print(",");
			ctx.print(StringUtils.format(entry.getWindHeading(), "000"));
			ctx.print(",");
			ctx.print(StringUtils.format(entry.getFuelFlow(), "###0"));
			ctx.print(",");
			ctx.print(StringUtils.format(entry.getG(), "#0.000"));
			ctx.print(",");
			ctx.print(StringUtils.format(entry.getAOA(), "##0.000"));
			ctx.print(",");
			ctx.print(entry.isFlagSet(ACARSFlags.FLAG_AP_NAV) ? "NAV," : ",");
			ctx.print(entry.isFlagSet(ACARSFlags.FLAG_AP_HDG) ? "HDG," : ",");
			ctx.print(entry.isFlagSet(ACARSFlags.FLAG_AP_APR) ? "APR," : ",");
			ctx.print(entry.isFlagSet(ACARSFlags.FLAG_AP_ALT) ? "ALT," : ",");
			if (entry.isFlagSet(ACARSFlags.FLAG_AT_IAS)) {
				ctx.println("IAS");
			} else if (entry.isFlagSet(ACARSFlags.FLAG_AT_MACH)) {
				ctx.println("MACH");
			} else {
				ctx.println("");
			}
		}

		// Write the response
		try {
			ctx.getResponse().setContentType("text/csv");
			ctx.getResponse().setHeader("Content-disposition", "attachment; filename=acars" + id + ".csv");
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error");
		}

		// Write success code
		return SC_OK;
	}

	/**
	 * Returns if the Web Service requires authentication.
	 * @return TRUE
	 */
	public final boolean isSecure() {
		return true;
	}
}