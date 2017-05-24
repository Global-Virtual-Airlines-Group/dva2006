// Copyright 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.Simulator;
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
 * @version 7.4
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
			List<Runway> toRwys = rwdao.getPopularRunways(a, null, true); toRwys.sort(rC);
			List<Runway> ldgRwys = rwdao.getPopularRunways(null, a, false); ldgRwys.sort(rC);
			
			// Check for invalid runways
			Collection<Runway> invalidRwys = new LinkedHashSet<Runway>();
			toRwys.stream().filter(r -> !GeoUtils.isValid(r)).forEach(r -> invalidRwys.add(r));
			ldgRwys.stream().filter(r -> !GeoUtils.isValid(r)).forEach(r -> invalidRwys.add(r));
			toRwys.removeAll(invalidRwys); ldgRwys.removeAll(invalidRwys);
			
			// Save runways
			ctx.setAttribute("toRwys", toRwys, REQUEST);
			ctx.setAttribute("ldgRwys", ldgRwys, REQUEST);
			ctx.setAttribute("invalidRwys", invalidRwys, REQUEST);

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
		ZonedDateTime zdt = ZonedDateTime.of(LocalDateTime.now(), a.getTZ().getZone());
		ctx.setAttribute("sunrise", SunriseSunset.getSunrise(a, zdt), REQUEST);
		ctx.setAttribute("sunset", SunriseSunset.getSunset(a, zdt), REQUEST);
		ctx.setAttribute("localTime", zdt, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/navdata/airportInfo.jsp");
		result.setSuccess(true);
	}
}