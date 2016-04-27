// Copyright 2006, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;

import org.deltava.beans.schedule.*;
import org.deltava.commands.*;
import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display a route map of Schedule entries.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class RouteMapCommand extends AbstractCommand {
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the airlines for this web application
		Collection<Airline> airlines = new TreeSet<Airline>();
		Map<String, Airline> allAirlines = SystemData.getAirlines();
		for (Iterator<Airline> i = allAirlines.values().iterator(); i.hasNext();) {
			Airline a = i.next();
			if (a.getActive())
				airlines.add(a);
		}
		
		// Init the airports map and split by airline
		Collection<Airport> allAirports = SystemData.getAirports().values();
		Map<Airline, Collection<Airport>> results = new HashMap<Airline, Collection<Airport>>();
		for (Iterator<Airport> i = allAirports.iterator(); i.hasNext(); ) {
			Airport a = i.next();
			for (Iterator<String> ai = a.getAirlineCodes().iterator(); ai.hasNext(); ) {
				Airline al = SystemData.getAirline(ai.next());
				if (airlines.contains(al))
					CollectionUtils.addMapCollection(results, al, a);
			}
		}
		
		// Save the map center
		ctx.setAttribute("mapCenter", new GeoPosition(SystemData.getDouble("airline.location.lat", 40), SystemData.getDouble("airline.location.lng", -85)), REQUEST);
		
		// Save the airport/color maps in the request
		ctx.setAttribute("airlines", airlines, REQUEST);
		ctx.setAttribute("airportMap", results, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/routeMap.jsp");
		result.setSuccess(true);
	}
}