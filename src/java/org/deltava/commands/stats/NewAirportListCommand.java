// Copyright 2011, 2012, 2015, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display airports a Pilot has not flown to.
 * @author Luke
 * @version 8.7
 * @since 4.0
 */

public class NewAirportListCommand extends AbstractCommand {

	/**
	 * Helper class for connecting airport lists.
	 */
	public class ConnectingAirportList extends ArrayList<Airport> {
		
		private final boolean _isSource;
		
		ConnectingAirportList(boolean isSource, Collection<Airport> airports) {
			super(airports);
			_isSource = isSource;
		}
		
		public boolean isSource() {
			return _isSource;
		}
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			for (Iterator<Airport> i = iterator(); i.hasNext(); ) {
				Airport a = i.next();
				buf.append(a.toString());
				if (i.hasNext())
					buf.append(", ");
			}
			
			return buf.toString();
		}
	}
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the default airline
		String defaultCode = SystemData.get("airline.code");
		Collection<Airport> myAirports = new HashSet<Airport>();
		boolean doMap = "map".equals(ctx.getCmdParameter(OPERATION, null));
		
		// Get the user ID
		int userID = ctx.getUser().getID();
		if ((ctx.isUserInRole("PIREP") || ctx.isUserInRole("HR")) && (ctx.getID() != 0))
			userID = ctx.getID();
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(userID);
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + userID);
			
			ctx.setAttribute("pilot", p, REQUEST);
			if (doMap)
				ctx.setAttribute("mapCenter", SystemData.getAirport(p.getHomeAirport()), REQUEST);

			// Load airports
			GetFlightReports frdao = new GetFlightReports(con);
			Collection<? extends RoutePair> routes = frdao.getRoutePairs(userID, 0);
			routes.stream().flatMap(rp -> List.of(rp.getAirportD(), rp.getAirportA()).stream()).forEach(myAirports::add);
			
			// Add academy airports
			GetSchedule sdao = new GetSchedule(con);
			myAirports.addAll(sdao.getAcademyAirports());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Get all airports and remove those we've visited or which aren't active
		Map<String, Collection<Airport>> airports = new LinkedHashMap<String, Collection<Airport>>();
		airports.put(defaultCode, new LinkedHashSet<Airport>());
		Collection<Airport> allAirports = new TreeSet<Airport>(SystemData.getAirports().values());
		for (Airport a : allAirports) {
			if (myAirports.contains(a) || a.getAirlineCodes().isEmpty())
				continue;
			
			// Sort the airlines for the airport, default first
			Collection<String> apAirlines = new LinkedHashSet<String>();
			if (a.hasAirlineCode(defaultCode))
				apAirlines.add(defaultCode);
			apAirlines.addAll(a.getAirlineCodes());
			
			// Get the first airline and its group
			String alCode = apAirlines.iterator().next();
			Collection<Airport> aps = airports.get(alCode);
			if (aps == null) {
				aps = new LinkedHashSet<Airport>();
				airports.put(alCode, aps);
			}
			
			aps.add(a);
		}
		
		// Check in case we've visited all airports for the default airline
		Collection<Airport> defaultAirlineList = airports.get(defaultCode);
		if (defaultAirlineList.isEmpty())
			airports.remove(defaultCode);
		
		// Load source airports
		Map<Airport, ConnectingAirportList> srcAirports = new HashMap<Airport, ConnectingAirportList>();
		try {
			Connection con = ctx.getConnection();
			GetScheduleAirport apdao = new GetScheduleAirport(con);
			for (Collection<Airport> aps : airports.values()) {
				for (Airport a : aps) {
					Collection<Airport> cAps = apdao.getConnectingAirports(a, false, null);
					if (cAps.isEmpty()) {
						cAps = apdao.getConnectingAirports(a, true, null);
						srcAirports.put(a, new ConnectingAirportList(true, cAps));
					} else 
						srcAirports.put(a, new ConnectingAirportList(false, cAps));
				}
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Get active airlines
		Collection<Airline> airlines = new TreeSet<Airline>();
		for (String alCode : airports.keySet())
			airlines.add(SystemData.getAirline(alCode));
		
		// Save request attributes
		ctx.setAttribute("airlines", airlines, REQUEST);
		ctx.setAttribute("airports", airports, REQUEST);
		ctx.setAttribute("srcAirports", srcAirports, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/missedAirport" + (doMap ? "Map.jsp" : "s.jsp"));
		result.setSuccess(true);
	}
}