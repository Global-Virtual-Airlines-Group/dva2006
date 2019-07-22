// Copyright 2014, 2015, 2017, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.sql.Connection;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.FlightInfo;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.comparators.FlightReportComparator;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display route tracks between airports.
 * @author Luke
 * @version 8.6
 * @since 5.4
 */

public class MyTrackService extends WebService {
	
	private static final int MAX_FLIGHTS = 16;

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the user ID
		int userID = ctx.getUser().getID();
		int id = StringUtils.parse(ctx.getParameter("id"), 0);
		if ((ctx.isUserInRole("PIREP") || ctx.isUserInRole("HR")) && (id > 0))
			userID = id;
		
		// Get the airprot
		Airport a = SystemData.getAirport(ctx.getParameter("icao"));
		if (a == null)
			throw error(SC_NOT_FOUND, "Invalid Airport - " + ctx.getParameter("icao"));

		Map<FlightInfo, Collection<GeospaceLocation>> rts = new LinkedHashMap<FlightInfo, Collection<GeospaceLocation>>();
		try {
			Connection con = ctx.getConnection();
			GetFlightReports frdao = new GetFlightReports(con);
			List<FlightReport> flights = new ArrayList<FlightReport>();
			
			// Get the Flight Reports originating at the airport
			ScheduleSearchCriteria ssd = new ScheduleSearchCriteria("DATE DESC");
			ssd.setAirportD(a);
			flights.addAll(frdao.getByPilot(userID, ssd));
			
			// Get the Flight Reports arriving at the airport
			ScheduleSearchCriteria ssa = new ScheduleSearchCriteria("DATE DESC");
			ssa.setAirportA(a);
			flights.addAll(frdao.getByPilot(userID, ssa));
			
			// Filter out non-ACARS and sort
			flights.removeIf(fr -> (!fr.hasAttribute(FlightReport.ATTR_ACARS) || (fr.getStatus() != FlightStatus.OK)));
			flights.sort(new FlightReportComparator(0).reversed());
			if (flights.size() > MAX_FLIGHTS)
				flights = flights.subList(0, MAX_FLIGHTS);
			
			// Get the Track data
			GetACARSData addao = new GetACARSData(con);
			GetACARSPositions apdao = new GetACARSPositions(con);
			for (FlightReport fr : flights) {
				FlightInfo fi = addao.getInfo(fr.getDatabaseID(DatabaseID.ACARS));
				if (fi != null)
					rts.put(fi, apdao.getRouteEntries(fi.getID(), false, fi.getArchived()));
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Generate the output
		JSONObject jo = new JSONObject();
		for (Map.Entry<FlightInfo, Collection<GeospaceLocation>> me : rts.entrySet()) {
			JSONObject fo = new JSONObject();
			fo.put("isDST", (me.getKey().getAirportA().equals(a)));
			GeoLocation last = null;
			for (GeoLocation loc : me.getValue()) {
				int dst = loc.distanceTo(last);
				if ((last == null) || (dst > 20)) {
					last = loc;
					fo.append("trk", JSONUtils.format(loc));
				}
			}
			
			JSONUtils.ensureArrayPresent(fo, "trk");
			jo.append("routes", fo);
		}
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.setExpiry(2700);
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