// Copyright 2012, 2015, 2016, 2017, 2021, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.time.*;
import java.sql.Connection;

import org.json.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.acars.*;
import org.deltava.beans.flight.*;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to display flight data in JSON format. 
 * @author Luke
 * @version 11.6
 * @since 5.0
 */

public class FlightStatsService extends WebService {
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		Collection<? extends GeoLocation> routePoints = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the Flight Report
			GetFlightReports frdao = new GetFlightReports(con);
			FlightReport fr = frdao.get(StringUtils.parse(ctx.getParameter("id"), 0), ctx.getDB());
			if (fr == null)
				throw error(SC_NOT_FOUND, "Invalid Flight Report", false);
			
			// Get the position data
			GetACARSPositions dao = new GetACARSPositions(con);
			FlightInfo info = dao.getInfo(fr.getDatabaseID(DatabaseID.ACARS));
			if (info == null)
				routePoints = Collections.emptyList();
			else if ((info.getFDR() == Recorder.XACARS) && !info.getArchived())
				routePoints = dao.getXACARSEntries(info.getID());
			else
				routePoints = dao.getRouteEntries(info.getID(), true, info.getArchived());
		} catch (ArchiveValidationException ave) {
			throw error(SC_INTERNAL_SERVER_ERROR, ave.getMessage(), true);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Determine if sim time went backwards
		boolean fwdTime = true; Instant lt = Instant.EPOCH, ft = null;
		for (Iterator<? extends GeoLocation> i = routePoints.iterator(); i.hasNext() && fwdTime; ) {
			GeoLocation loc = i.next();
			if (loc instanceof ACARSRouteEntry ae) {
				ft = (ft == null) ? ae.getSimUTC() : ft;
				fwdTime &= (ae.getSimUTC() != null) && !ae.getSimUTC().isBefore(lt); // equal or after
				lt = ae.getSimUTC();
			}
		}
		
		// Determine how far forward time went
		if (fwdTime && (ft != null)) {
			Duration td = Duration.between(ft, lt);
			fwdTime = !td.isNegative() && (td.toHours() < 24);
		}
		
		// Go through the rows
		JSONObject jo = new JSONObject();
		int maxSpeed = 0; int maxAlt = 0;
		for (GeoLocation loc : routePoints) {
			if (loc instanceof RouteEntry re) {
				JSONArray je = new JSONArray();
				if (re instanceof ACARSRouteEntry ae) {
					je.put(fwdTime ? ae.getSimUTC().toEpochMilli() : ae.getDate().toEpochMilli());
					je.put(re.getGroundSpeed());
					je.put(Math.max(0, re.getAltitude()));	
					je.put(Math.max(0, ae.getAltitude() - ae.getRadarAltitude()));
				} else {
					je.put(re.getDate().toEpochMilli());
					je.put(re.getGroundSpeed());
					je.put(Math.max(0, re.getAltitude()));	
				}
				
				jo.append("data", je);
				maxSpeed = Math.max(maxSpeed, re.getGroundSpeed());
				maxAlt = Math.max(maxAlt, re.getAltitude());
			}
		}
		
		// Adjust maximum speed and altitude
		maxAlt = (maxAlt + 10000 - (maxAlt % 10000));
		maxSpeed = (maxSpeed + 100 - (maxSpeed % 100));
		int altInterval = maxAlt / 5;
		jo.put("maxAlt", maxAlt);
		jo.put("maxSpeed", maxSpeed);
		jo.put("altInterval", altInterval);
		jo.put("isSimTime", fwdTime);
		for (int x = 0; x <= 5; x++)
			jo.append("altIntervals", Integer.valueOf(altInterval * x));
		
		// Dump to the output stream
		JSONUtils.ensureArrayPresent(jo, "data");
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.setExpiry(3600);
			ctx.println(jo.toString());
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
	
	@Override
	public final boolean isLogged() {
		return false;
	}
}