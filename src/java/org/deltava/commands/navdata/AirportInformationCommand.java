// Copyright 2015, 2016, 2017, 2019, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.Simulator;
import org.deltava.beans.acars.TaxiTime;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.wx.METAR;

import org.deltava.commands.*;
import org.deltava.comparators.*;
import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display Airport runway and gate information.
 * @author Luke
 * @version 10.2
 * @since 6.3
 */

public class AirportInformationCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the airport
		Airport a = SystemData.getAirport((String) ctx.getCmdParameter(ID, null));
		if (a == null)
			throw notFoundException("Unknown Airport - " + ctx.getCmdParameter(ID, null));
		
		try {
			Connection con = ctx.getConnection();
			
			// Get weather
			GetWeather wxdao = new GetWeather(con);
			METAR m = wxdao.getMETAR(a, 25);
			ctx.setAttribute("wx", m, REQUEST);
			
			// Get runway data and build bounding box
			GetNavData nddao = new GetNavData(con);
			Collection<Runway> allRwys = nddao.getRunways(a, Simulator.P3Dv4);
			ctx.setAttribute("rwys", allRwys.isEmpty() ? Collections.singleton(a) : allRwys.stream().map(GeoPosition::new).collect(Collectors.toSet()), REQUEST);
			int maxLength = allRwys.stream().mapToInt(Runway::getLength).max().orElse(0);
			
			// Load takeoff/landing runways
			GetACARSRunways rwdao = new GetACARSRunways(con);
			List<RunwayUsage> allDRwys = rwdao.getPopularRunways(a, true); 
			List<RunwayUsage> allARwys = rwdao.getPopularRunways(a, false);
			
			// Filter arrival/departure runways - first select those available due to winds, then other popular runways
			UsagePercentFilter rf = new UsagePercentFilter(22);
			UsageWindFilter wf = new UsageWindFilter(22, -6);
			Collection<Runway> dRwys = new LinkedHashSet<Runway>(wf.filter(allDRwys));
			Collection<Runway> aRwys = new LinkedHashSet<Runway>(wf.filter(allARwys));
			Collection<? extends Runway> validRunways = CollectionUtils.join(dRwys, aRwys);
			dRwys.addAll(rf.filter(allDRwys)); aRwys.addAll(rf.filter(allARwys));
			Collection<String> rwyIDs = CollectionUtils.join(dRwys, aRwys).stream().map(Runway::getName).collect(Collectors.toSet());
			allRwys.removeIf(r -> rwyIDs.contains(r.getName()));
			
			// Determine valid runways for winds
			RunwayComparator rc = new RunwayComparator(0, 0, true); // only sort by useCount
			ctx.setAttribute("departureRwys", CollectionUtils.sort(dRwys, rc), REQUEST);
			ctx.setAttribute("arrivalRwys", CollectionUtils.sort(aRwys, rc), REQUEST);
			ctx.setAttribute("validRunways", validRunways.stream().map(Runway::getName).collect(Collectors.toSet()), REQUEST);
			
			// Get remaining runways
			ctx.setAttribute("odRwyStats", CollectionUtils.createMap(allDRwys, Runway::getName), REQUEST);
			ctx.setAttribute("oaRwyStats", CollectionUtils.createMap(allARwys, Runway::getName), REQUEST);
			ctx.setAttribute("otherRunways", allRwys, REQUEST);
			
			// Get chart availability
			GetChart chdao = new GetChart(con);
			Collection<Chart.Type> chartTypes = chdao.getCharts(a).stream().map(Chart::getType).collect(Collectors.toSet());
			ctx.setAttribute("chartTypes", chartTypes, REQUEST);
			
			// Get taxi times
			GetACARSTaxiTimes ttdao = new GetACARSTaxiTimes(con);
			TaxiTime ttAvg = ttdao.getTaxiTime(a);
			TaxiTime ttYr = ttdao.getTaxiTime(a, LocalDate.now().getYear());
			
			// Get Aircraft for runway length
			String aCode = SystemData.get("airline.code");
			GetAircraft acdao = new GetAircraft(con);
			Collection<Aircraft> allAC = acdao.getAircraftTypes(aCode);
			Collection<Aircraft> validAC = allAC.stream().filter(ac -> ac.isUsed(aCode) && aircraftRunwayFilter(ac.getOptions(aCode), maxLength)).collect(Collectors.toList());
			
			// Calculate whether we show valid equipment, or invalid (to reduce size of list)
			double validRatio = validAC.size() * 1.0d / allAC.size();
			if (validRatio >= 0.5) {
				Collection<Aircraft> invalidAC = allAC.stream().filter(ac -> !validAC.contains(ac)).collect(Collectors.toList());
				ctx.setAttribute("invalidAC", invalidAC, REQUEST);
			} else
				ctx.setAttribute("validAC", validAC, REQUEST);
			
			// Save runways
			ctx.setAttribute("toRwys", allDRwys, REQUEST);
			ctx.setAttribute("ldgRwys", allARwys, REQUEST);
			ctx.setAttribute("maxRwyLength", Integer.valueOf(maxLength), REQUEST);
			
			// Save taxi times
			ctx.setAttribute("taxiTime", ttAvg, REQUEST);
			ctx.setAttribute("taxiTimeCY", ttYr, REQUEST);
			
			// Save operations
			GetRawSchedule rsdao = new GetRawSchedule(con);
			ctx.setAttribute("dDays", rsdao.getDays(null, a, false), REQUEST);
			ctx.setAttribute("aDays", rsdao.getDays(null, a, true), REQUEST);
			ctx.setAttribute("schedAirlines", rsdao.getAirlines(null, a), REQUEST);
			
			// Save in request
			ctx.setAttribute("airport", a, REQUEST);
			ctx.setAttribute("airlines", a.getAirlineCodes().stream().map(c -> SystemData.getAirline(c)).filter(al -> !al.getHistoric()).collect(Collectors.toCollection(TreeSet::new)), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Load active airports
		Collection<Airport> airports = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME)); 
		airports.addAll(SystemData.getAirports().values().stream().filter(ap -> !ap.getAirlineCodes().isEmpty()).collect(Collectors.toList()));
		ctx.setAttribute("airports", airports, REQUEST);
		
		// Calculate sunrise / sunset
		ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.now(), a.getTZ().getZone());
		ctx.setAttribute("sunrise", SunriseSunset.getSunrise(a, zdt), REQUEST);
		ctx.setAttribute("sunset", SunriseSunset.getSunset(a, zdt), REQUEST);
		ctx.setAttribute("localTime", zdt, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/navdata/airportInfo.jsp");
		result.setSuccess(true);
	}
		
	private static boolean aircraftRunwayFilter(AircraftPolicyOptions opts, int maxLength) {
		if (maxLength == 0) return true;
		if ((maxLength > 0) && (opts.getTakeoffRunwayLength() > 0) && (opts.getTakeoffRunwayLength() > maxLength)) return false;
		return ((opts.getLandingRunwayLength() == 0) || (opts.getLandingRunwayLength() <= maxLength));
	}
}