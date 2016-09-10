// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.util.*;
import java.time.Instant;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.acars.*;
import org.deltava.beans.flight.Recorder;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.StringUtils;

import static org.gvagroup.acars.ACARSFlags.*;

/**
 * A Web Service to return ACARS flight data parameters.
 * @author Luke
 * @version 7.2
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
			else if ((info.getFDR() == Recorder.XACARS) && !info.getArchived())
				routeData.addAll(dao.getXACARSEntries(id));
			else
				routeData.addAll(dao.getRouteEntries(id, info.getArchived()));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// Write the CSV header
		if (info.getFDR() != Recorder.XACARS) {
			ctx.println("Date/Time,Latitude,Longitude,Altitude,Heading,Air Speed,Ground Speed,Mach,Vertical Speed,N1,N2,Bank,Pitch,Flaps,"
				+ "WindSpeed,WindHdg,Temperature,Pressure,Visibility,FuelFlow,Fuel,Gs,AOA,AP,ALT,AT,FrameRate,VAS,NAV1,NAV2,COM1,ATC1,COM2,ATC2,WARN");
		} else
			ctx.println("Date/Time,Latitude,Longitude,Altitude,Heading,Air Speed,Ground Speed,Mach,WindSpeed,WindHdg,Fuel");

		// Format the ACARS data
		for (RouteEntry entry : routeData) {
			if (entry instanceof ACARSRouteEntry)
				ctx.println(format((ACARSRouteEntry) entry));
			else if (entry instanceof XARouteEntry)
				ctx.println(format((XARouteEntry) entry));
		}

		// Write the response
		try {
			ctx.setContentType("text/csv", "utf-8");
			ctx.setHeader("Content-disposition", "attachment; filename=acars" + id + ".csv");
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}

		return SC_OK;
	}
	
	/*
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

	/*
	 * Helper method to format an ACARS position entry.
	 */
	private static String format(ACARSRouteEntry entry) {
		Instant i = (entry.getSimUTC() == null) ? entry.getDate() : entry.getSimUTC();
		StringBuilder buf = new StringBuilder(StringUtils.format(i, "MM/dd/yyyy HH:mm:ss"));
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
		buf.append((entry.getTemperature() == 0) ? "" : String.valueOf(entry.getTemperature()));
		buf.append(',');
		buf.append((entry.getPressure() == 0) ? "" : StringUtils.format(entry.getPressure() / 1000.0, "##0.00"));
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
		if (entry.isFlagSet(FLAG_AP_NAV))
			buf.append("NAV");
		else if (entry.isFlagSet(FLAG_AP_HDG))
			buf.append("HDG");
		else if (entry.isFlagSet(FLAG_AP_APR))
			buf.append("APR");
		else if (entry.isFlagSet(FLAG_AP_LNAV))
			buf.append("LNAV");
		
		buf.append(',');
		buf.append(entry.isFlagSet(FLAG_AP_ALT) ? "ALT," : ",");
		if (entry.isFlagSet(FLAG_AT_IAS))
			buf.append("IAS");
		else if (entry.isFlagSet(FLAG_AT_MACH))
			buf.append("MACH");
		else if (entry.isFlagSet(FLAG_AT_FLCH))
			buf.append("FLCH");
		else if (entry.isFlagSet(FLAG_AT_VNAV))
			buf.append("VNAV");
		
		buf.append(',');
		buf.append(String.valueOf(entry.getFrameRate()));
		buf.append(',');
		if (entry.getVASFree() > 0)
			buf.append(entry.getVASFree() / 1024);
		buf.append(',');
		buf.append(entry.getNAV1());
		buf.append(',');
		buf.append(entry.getNAV2());
		buf.append(',');
		if (entry.getATC1() != null) {
			buf.append(entry.getCOM1());
			buf.append(',');
			buf.append(entry.getATC1().getCallsign());
			buf.append(',');
		} else
			buf.append(",,");
		
		if (entry.getATC2() != null) {
			buf.append(entry.getCOM2());
			buf.append(',');
			buf.append(entry.getATC2().getCallsign());
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