// Copyright 2017, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.json.*;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.stats.*;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display Airports needed to complete Accomplishments. 
 * @author Luke
 * @version 10.4
 * @since 7.2
 */

public class AccomplishmentAirportMapCommand extends AbstractCommand {
	
	private class AirportMapEntry implements IconMapEntry, ICAOAirport, Comparable<AirportMapEntry> {
		private final Airport _a;
		
		AirportMapEntry(Airport a) {
			super();
			_a = a;
		}
		
		@Override
		public String getICAO() {
			return _a.getICAO();
		}
		
		@Override
		public double getLatitude() {
			return _a.getLatitude();
		}
		
		@Override
		public double getLongitude() {
			return _a.getLongitude();
		}
		
		@Override
		public int getAltitude() {
			return _a.getAltitude();
		}
		
		@Override
		public int getIconCode() {
			return _a.getIconCode();
		}
		
		@Override
		public int getPaletteCode() {
			return _a.getPaletteCode();
		}
		
		@Override
		public String getInfoBox() {
			StringBuilder buf = new StringBuilder(_a.getInfoBox());
			buf.setLength(buf.length() - 6); // strip closing div tag
			buf.append("<br /><br /><span class=\"small\">Airlines served:<br />");
			_a.getAirlineCodes().stream().map(SystemData::getAirline).filter(Objects::nonNull).forEach(al -> buf.append(al.getName()).append("<br />"));
			buf.append("</div>");
			return buf.toString();
		}

		@Override
		public int compareTo(AirportMapEntry ae2) {
			return _a.compareTo(ae2._a);
		}
	}
	
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
		
        Map<String, Collection<AirportMapEntry>> missingAirports = new HashMap<String, Collection<AirportMapEntry>>();
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
			flights.forEach(helper::add);
			
			// Map the missing airports to the accomplishments
			accs.stream().filter(Accomplishment::getActive).forEach(a -> missingAirports.put(a.getComboAlias(), helper.missing(a).stream().map(ap -> new AirportMapEntry((Airport) ap)).collect(Collectors.toList())));
			
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
		Collection<AirportMapEntry> allAirports = new TreeSet<AirportMapEntry>();
		for (Map.Entry<String, Collection<AirportMapEntry>> me : missingAirports.entrySet()) {
			allAirports.addAll(me.getValue());
			JSONArray ja = new JSONArray();
			me.getValue().forEach(a -> ja.put(a.getICAO()));
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