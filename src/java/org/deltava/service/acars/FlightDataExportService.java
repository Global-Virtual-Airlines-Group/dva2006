// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.util.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.acars.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.StringUtils;

import static org.gvagroup.acars.ACARSFlags.*;

/**
 * A Web Service to return ACARS flight data parameters.
 * @author Luke
 * @version 4.2
 * @since 1.0
 */

public class FlightDataExportService extends WebService {

	/**
	 * Executes the Web Service, writing ACARS Flight data in CSV format.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the ACARS Flight ID
		int id = StringUtils.parse(ctx.getParameter("id"), 0);
		if (id < 1)
			return SC_NOT_FOUND;

		// Get the ACARS data
		FlightInfo info = null;
		List<RouteEntry> routeData = new ArrayList<RouteEntry>(); 
		try {
			GetACARSPositions dao = new GetACARSPositions(ctx.getConnection());
			info = dao.getInfo(id);
			if (info == null)
				return SC_NOT_FOUND;
			else if (info.isXACARS())
				routeData.addAll(dao.getXACARSEntries(id));
			else
				routeData.addAll(dao.getRouteEntries(id, info.getArchived()));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Write the CSV header
		if (!info.isXACARS()) {
			ctx.print("Date/Time,Latitude,Longitude,Altitude,Heading,Air Speed,Ground Speed,Mach,Vertical Speed,N1,N2,Bank,Pitch,Flaps,");
			ctx.println("WindSpeed,WindHdg,Visibility,FuelFlow,Fuel,Gs,AOA,NAV,HDG,APR,ALT,AT,FrameRate,COM1,ATC,WARN");
		} else
			ctx.println("Date/Time,Latitude,Longitude,Altitude,Heading,Air Speed,Ground Speed,Mach,WindSpeed,WindHdg,Fuel");

		// Format the ACARS data
		for (Iterator<? extends RouteEntry> i = routeData.iterator(); i.hasNext();) {
			RouteEntry entry = i.next();
			if (entry instanceof ACARSRouteEntry)
				ctx.println(format((ACARSRouteEntry) entry));
			else if (entry instanceof XARouteEntry)
				ctx.println(format((XARouteEntry) entry));
		}

		// Write the response
		try {
			ctx.setContentType("text/csv", "UTF-8");
			ctx.getResponse().setHeader("Content-disposition", "attachment; filename=acars" + id + ".csv");
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		// Write success code
		return SC_OK;
	}
	
	/**
	 * Helper method to format an XACARS position entry.
	 */
	private static String format(XARouteEntry entry) {
		StringBuilder buf = new StringBuilder(StringUtils.format(entry.getDate(), "MM/dd/yyyy HH:mm:ss"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getLatitude(), "##0.0000"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getLongitude(), "##0.0000"));
		buf.append(',');
		buf.append(String.valueOf(entry.getAltitude()));
		buf.append(',');
		buf.append(StringUtils.format(entry.getHeading(), "000"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getAirSpeed(), "##0"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getGroundSpeed(), "##0"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getMach(), "#.000"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getVerticalSpeed(), "##0"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getWindSpeed(), "##0"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getWindHeading(), "000"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getFuelRemaining(), "###0"));
		return buf.toString();
	}

	/**
	 * Helper method to format an ACARS position entry.
	 */
	private static String format(ACARSRouteEntry entry) {
		StringBuilder buf = new StringBuilder(StringUtils.format(entry.getDate(), "MM/dd/yyyy HH:mm:ss"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getLatitude(), "##0.0000"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getLongitude(), "##0.0000"));
		buf.append(',');
		buf.append(String.valueOf(entry.getAltitude()));
		buf.append(',');
		buf.append(StringUtils.format(entry.getHeading(), "000"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getAirSpeed(), "##0"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getGroundSpeed(), "##0"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getMach(), "#.000"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getVerticalSpeed(), "##0"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getN1(), "##0.0"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getN2(), "##0.0"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getBank(), "##0.0"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getPitch(), "##0.0"));
		buf.append(',');
		buf.append((entry.getFlaps() == 0) ? "" : String.valueOf(entry.getFlaps()));
		buf.append(',');
		buf.append(StringUtils.format(entry.getWindSpeed(), "##0"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getWindHeading(), "000"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getVisibility(), "#0.00"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getFuelFlow(), "###0"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getFuelRemaining(), "###0"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getG(), "#0.000"));
		buf.append(',');
		buf.append(StringUtils.format(entry.getAOA(), "##0.000"));
		buf.append(',');
		buf.append(entry.isFlagSet(FLAG_AP_NAV) ? "NAV," : ",");
		buf.append(entry.isFlagSet(FLAG_AP_HDG) ? "HDG," : ",");
		buf.append(entry.isFlagSet(FLAG_AP_APR) ? "APR," : ",");
		buf.append(entry.isFlagSet(FLAG_AP_ALT) ? "ALT," : ",");
		if (entry.isFlagSet(FLAG_AT_IAS))
			buf.append("IAS");
		else if (entry.isFlagSet(FLAG_AT_MACH))
			buf.append("MACH");
		
		buf.append(',');
		buf.append(String.valueOf(entry.getFrameRate()));
		buf.append(',');
		if (entry.getController() != null) {
			buf.append(entry.getCOM1());
			buf.append(',');
			buf.append(entry.getController().getCallsign());
			buf.append(',');
		} else
			buf.append(",,");
		
		if (entry.isFlagSet(FLAG_STALL))
			buf.append("STALL");
		else if (entry.isFlagSet(FLAG_OVERSPEED))
			buf.append("OVERSPEED");
		
		return buf.toString();
	}
}