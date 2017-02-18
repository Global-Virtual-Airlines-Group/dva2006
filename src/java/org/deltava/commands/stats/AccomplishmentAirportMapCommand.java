// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;

import org.json.*;

import org.deltava.beans.Pilot;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.stats.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display Airports needed to complete Accomplishments. 
 * @author Luke
 * @version 7.2
 * @since 7.2
 */

public class AccomplishmentAirportMapCommand extends AbstractCommand {

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Determine who to display
        int id = ctx.isUserInRole("HR") ? ctx.getID() : ctx.getUser().getID();
        if (id == 0)
        		id = ctx.getUser().getID();
        
        ctx.setAttribute("isOurs", Boolean.valueOf(id == ctx.getUser().getID()), REQUEST);
		
        Map<String, Collection<?>> missingAirports = new HashMap<String, Collection<?>>();
		try {
			Connection con = ctx.getConnection();
			
			// Load the pilot
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(id);
			if (p == null)
				throw notFoundException("Invalid Pilot ID - " + id);
			
			// Load all accomplishments involving airports
			GetAccomplishment adao = new GetAccomplishment(con);
			Collection<Accomplishment> accs = adao.getByUnit(AccomplishUnit.AIRPORTS);
			accs.addAll(adao.getByUnit(AccomplishUnit.AIRPORTA));
			accs.addAll(adao.getByUnit(AccomplishUnit.AIRPORTD));
			
			// Load the Pilot's Flight Reports
			AccomplishmentHistoryHelper helper = new AccomplishmentHistoryHelper(p);
			GetFlightReports frdao = new GetFlightReports(con);
			Collection<FlightReport> flights = frdao.getByPilot(p.getID(), null);
			frdao.getCaptEQType(flights);
			flights.forEach(fr -> helper.add(fr));
			
			// Map the missing airports to the accomplishments
			accs.forEach(a -> missingAirports.put(a.getComboAlias(), helper.missing(a)));
			
			// Save in request
			ctx.setAttribute("accs", accs, REQUEST);
			ctx.setAttribute("pilot", p, REQUEST);
			ctx.setAttribute("mapCenter", SystemData.getAirport(p.getHomeAirport()), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Convert to JSON
		JSONObject jo = new JSONObject();
		Collection<Airport> allAirports = new TreeSet<Airport>();
		for (Map.Entry<String, Collection<?>> me : missingAirports.entrySet()) {
			@SuppressWarnings("unchecked")
			Collection<Airport> airports = (Collection<Airport>) me.getValue();
			allAirports.addAll(airports);

			JSONArray ja = new JSONArray();
			airports.forEach(a -> ja.put(a.getICAO()));
			jo.put(me.getKey(), ja);
		}
		
		// Save in the request
		ctx.setAttribute("airports", allAirports, REQUEST);
		ctx.setAttribute("jsData", jo.toString(), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/accomplishmentAirportMap.jsp");
		result.setSuccess(true);
	}
}