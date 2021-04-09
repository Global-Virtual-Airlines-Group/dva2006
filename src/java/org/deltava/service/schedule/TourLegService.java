// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static javax.servlet.http.HttpServletResponse.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.format.DateTimeFormatterBuilder;

import org.json.*;

import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to calculate flight times for custom Flight Tour legs.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class TourLegService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the Airports
		Airport aD = SystemData.getAirport(ctx.getParameter("airportD"));
		Airport aA = SystemData.getAirport(ctx.getParameter("airportA"));
		if ((aD == null) || (aA == null))
			return SC_BAD_REQUEST;
		
		// Time parser init
		LocalDateTime today = LocalDateTime.now();
		DateTimeFormatterBuilder tfb = new DateTimeFormatterBuilder().appendPattern("HH:mm");
		tfb.parseDefaulting(ChronoField.YEAR_OF_ERA, today.get(ChronoField.YEAR_OF_ERA));
		tfb.parseDefaulting(ChronoField.DAY_OF_YEAR, today.getLong(ChronoField.DAY_OF_YEAR));
		
		// Build the schedule entry and convert to JSON
		ScheduleEntry se = new ScheduleEntry(SystemData.getAirline(ctx.getParameter("airline")), StringUtils.parse(ctx.getParameter("flight"), 1), StringUtils.parse(ctx.getParameter("leg"), 1));
		se.setEquipmentType(ctx.getParameter("eqType"));
		se.setAirportD(aD);
		se.setAirportA(aA);
		se.setTimeD(LocalDateTime.parse(ctx.getParameter("timeD"), tfb.toFormatter()));
		se.setTimeA(LocalDateTime.parse(ctx.getParameter("timeA"), tfb.toFormatter()));
		se.setSource(ScheduleSource.MANUAL);
		JSONObject jo = JSONUtils.format(se);
		
		// Validate the leg
		try {
			GetAircraft acdao = new GetAircraft(ctx.getConnection());
			Aircraft a = acdao.get(se.getEquipmentType());
			AircraftPolicyOptions opts = a.getOptions(SystemData.get("airline.code"));
			jo.put("rangeWarn", (se.getDistance() > opts.getRange()));
			jo.put("tRunwayWarn", (opts.getTakeoffRunwayLength() > se.getAirportD().getMaximumRunwayLength()));
			jo.put("lRunwayWarn", (opts.getLandingRunwayLength() > se.getAirportA().getMaximumRunwayLength()));

			// Check ETOPS
			ETOPSResult er = ETOPSHelper.classify(GeoUtils.greatCircle(se.getAirportD(), se.getAirportA(), 30));
			jo.put("etopsWarn", ETOPSHelper.isWarn(opts.getETOPS(), er.getResult()));
			
			// Save ETOPS data
			JSONObject eo = new JSONObject();
			eo.put("leg", er.getResult());
			eo.put("aircraft", opts.getETOPS());
			eo.put("msgs", new JSONArray(er.getMessages()));
			jo.put("etops", eo);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}

		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.println(jo.toString());
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
	
	@Override
	public boolean isLogged() {
		return false;
	}

	@Override
	public final boolean isSecure() {
		return true;
	}
}