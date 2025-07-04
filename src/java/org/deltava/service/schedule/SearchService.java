// Copyright 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.schedule;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.time.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.json.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to search the Flight Schedule.
 * @author Luke
 * @version 10.1
 * @since 9.0
 */

public class SearchService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Build the search criteria
		ScheduleSearchCriteria ssc = new ScheduleSearchCriteria("S.TIME_D");
		ssc.setAirline(SystemData.getAirline(ctx.getParameter("airline")));
		ssc.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
		ssc.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
		ssc.setMaxResults(Math.min(250, StringUtils.parse(ctx.getParameter("max"), 50)));
		ssc.setDistance(StringUtils.parse(ctx.getParameter("distance"), 0));
		ssc.setDistanceRange(StringUtils.parse(ctx.getParameter("distanceRange"), (ssc.getDistance() == 0) ? 0 : 250));
		ssc.setDBName(ctx.getDB());
		
		Collection<ScheduleEntry> results = new ArrayList<ScheduleEntry>();
		Collection<ScheduleSourceInfo> srcs = new ArrayList<ScheduleSourceInfo>();
		try {
			Connection con = ctx.getConnection();
			
			// Get effective dates
			GetRawSchedule rsdao = new GetRawSchedule(con);
			srcs.addAll(rsdao.getSources(true, ctx.getDB()));
			
			// Search the schedule
			GetScheduleSearch sdao = new GetScheduleSearch(con);
			sdao.setSources(srcs);
			results.addAll(sdao.search(ssc));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Filter sources
		Collection<ScheduleSource> includedSrcs = results.stream().map(ScheduleEntry::getSource).collect(Collectors.toSet());
		srcs.removeIf(src -> !includedSrcs.contains(src.getSource()));
		
		// Create the JSON document
		JSONObject ro = new JSONObject();
		JSONObject srco = new JSONObject();
		ro.put("created", Instant.now().toEpochMilli());
		ro.put("sources", srco);
		for (ScheduleSourceInfo srcInfo : srcs) {
			JSONObject so = new JSONObject();
			so.put("id", srcInfo.getSource().name());
			so.put("name", srcInfo.getSource().getDescription());
			so.put("importDate", srcInfo.getDate().toEpochMilli());
			so.put("effectiveDate", srcInfo.getEffectiveDate().toEpochSecond(LocalTime.MIDNIGHT, ZoneOffset.UTC) * 1000);
			so.put("isCurrent", srcInfo.getIsCurrent());
			srco.put(srcInfo.getSource().name(), so);
		}
		
		// Add the schedule entries
		results.forEach(se -> ro.accumulate("results", JSONUtils.format(se)));
		JSONUtils.ensureArrayPresent(ro, "results");
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.setExpiry(15);
			ctx.println(ro.toString());
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