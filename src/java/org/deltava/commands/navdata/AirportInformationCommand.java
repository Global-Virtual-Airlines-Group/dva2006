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
 * @version 10.0
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
			if (m == null)
				m = new METAR();
			
			// Get runway data
			Comparator<Runway> rC = new RunwayComparator(m.getWindDirection(), m.getWindSpeed());
			GetNavData nddao = new GetNavData(con);
			Map<String, Runway> allRwys = CollectionUtils.createMap(nddao.getRunways(a, Simulator.FSX), Runway::getName);
			
			// Build airport bounding box
			ctx.setAttribute("rwys", new ArrayList<Runway>(allRwys.values()), REQUEST);
			
			// Load takeoff/landing runways
			GetACARSRunways rwdao = new GetACARSRunways(con);
			List<Runway> toRwys = rwdao.getPopularRunways(a, true); toRwys.sort(rC);
			List<Runway> ldgRwys = rwdao.getPopularRunways(a, false); ldgRwys.sort(rC);
			
			// Get taxi times
			GetACARSTaxiTimes ttdao = new GetACARSTaxiTimes(con);
			TaxiTime ttAvg = ttdao.getTaxiTime(a);
			TaxiTime ttYr = ttdao.getTaxiTime(a, LocalDate.now().getYear());
			
			// Check for invalid runways
			Collection<Runway> invalidRwys = new LinkedHashSet<Runway>();
			toRwys.stream().filter(r -> !GeoUtils.isValid(r)).forEach(invalidRwys::add);
			ldgRwys.stream().filter(r -> !GeoUtils.isValid(r)).forEach(invalidRwys::add);
			toRwys.removeAll(invalidRwys); ldgRwys.removeAll(invalidRwys);
			int maxLength = allRwys.values().stream().mapToInt(Runway::getLength).max().orElse(0);
			
			// Get Aircraft for runway length
			String aCode = SystemData.get("airline.code");
			GetAircraft acdao = new GetAircraft(con);
			Collection<Aircraft> allAC = acdao.getAircraftTypes(aCode);
			Collection<Aircraft> validAC = allAC.stream().filter(ac -> ac.isUsed(aCode)).filter(ac -> aircraftRunwayFilter(ac.getOptions(aCode), maxLength)).collect(Collectors.toList());
			
			// Calculate whether we show valid equipment, or invalid (to reduce size of list)
			double validRatio = validAC.size() * 1.0d / allAC.size();
			if (validRatio >= 0.5) {
				Collection<Aircraft> invalidAC = allAC.stream().filter(ac -> !validAC.contains(ac)).collect(Collectors.toList());
				ctx.setAttribute("invalidAC", invalidAC, REQUEST);
			} else
				ctx.setAttribute("validAC", validAC, REQUEST);
			
			// Save runways
			ctx.setAttribute("toRwys", toRwys, REQUEST);
			ctx.setAttribute("ldgRwys", ldgRwys, REQUEST);
			ctx.setAttribute("invalidRwys", invalidRwys, REQUEST);
			
			// Save taxi times
			ctx.setAttribute("taxiTime", ttAvg, REQUEST);
			ctx.setAttribute("taxiTimeCY", ttYr, REQUEST);
			
			// Save destinations
			GetScheduleAirport sadao = new GetScheduleAirport(con);
			ctx.setAttribute("connectingAirports", sadao.getConnectingAirports(a, true, null), REQUEST);
			
			// Save operations
			GetRawSchedule rsdao = new GetRawSchedule(con);
			ctx.setAttribute("dDays", rsdao.getDays(null, a, false), REQUEST);
			ctx.setAttribute("aDays", rsdao.getDays(null, a, true), REQUEST);
			ctx.setAttribute("schedAirlines", rsdao.getAirlines(null, a), REQUEST);

			// Determine valid runways for winds
			Collection<String> validRunwayIDs = new HashSet<String>();
			Collection<String> rwyIDs = toRwys.stream().map(Runway::getName).collect(Collectors.toSet());
			rwyIDs.addAll(ldgRwys.stream().map(Runway::getName).collect(Collectors.toSet()));
			for (Iterator<Map.Entry<String, Runway>> i = allRwys.entrySet().iterator(); i.hasNext(); ) {
				Map.Entry<String, Runway> me = i.next();
				if (rwyIDs.contains(me.getKey()))
					i.remove();
				
				double wDelta = GeoUtils.delta(me.getValue().getHeading(), m.getWindDirection());
				double hW = StrictMath.cos(Math.toRadians(wDelta)) * m.getWindSpeed();
				if (hW >= 0)
					validRunwayIDs.add(me.getKey());
			}
			
			// Save in request
			ctx.setAttribute("airport", a, REQUEST);
			ctx.setAttribute("runways", allRwys.values(), REQUEST);
			ctx.setAttribute("validRunways", validRunwayIDs, REQUEST);
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
		
		if ((opts.getTakeoffRunwayLength() > 0) && (opts.getTakeoffRunwayLength() > maxLength))
			return false;
		
		return ((opts.getLandingRunwayLength() == 0) || (opts.getLandingRunwayLength() <= maxLength));
	}
}