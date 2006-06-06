// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;

import org.deltava.beans.schedule.*;
import org.deltava.commands.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display a route map of Schedule entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class RouteMapCommand extends AbstractCommand {
	
	private static final String[] COLORS = {"blue", "black", "green", "red", "purple", "grey", "brown", "orange", "pink", "yellow"};

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
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
				if (airlines.contains(al)) {
					Collection<Airport> apList = results.get(al);
					if (apList == null)
						apList = new TreeSet<Airport>();
				
					apList.add(a);
				}
			}
		}
		
		// Map the results to icon colors
		int ofs = 0;
		Map<String, String> aColors = new HashMap<String, String>();
		for (Iterator<Airline> i = results.keySet().iterator(); i.hasNext(); ) {
			Airline a = i.next();
			aColors.put(a.getCode(), COLORS[ofs]);
			ofs++;
			if (ofs == COLORS.length)
				ofs = 0; 
		}
		
		// Order the airlines, with ours first
		List<Airline> sortedAirlines = new ArrayList<Airline>(airlines);
		ofs = sortedAirlines.indexOf(SystemData.getAirline(SystemData.get("airline.code")));
		if (ofs != -1) {
			Airline a = sortedAirlines.get(ofs);
			sortedAirlines.remove(ofs);
			sortedAirlines.add(0, a);
		}
		
		// Save the map center
		ctx.setAttribute("mapCenter", new GeoPosition(SystemData.getDouble("airline.location.lat", 40), 
				SystemData.getDouble("airline.location.lng", -85)), REQUEST);
		
		// Save the airport/color maps in the request
		ctx.setAttribute("airlines", sortedAirlines, REQUEST);
		ctx.setAttribute("airportMap", results, REQUEST);
		ctx.setAttribute("airportColors", aColors, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/routeMap.jsp");
		result.setSuccess(true);
	}
}