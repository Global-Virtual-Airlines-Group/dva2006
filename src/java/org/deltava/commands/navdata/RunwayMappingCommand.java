// Copyright 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.util.*;
import java.sql.Connection;

import org.json.*;

import org.deltava.beans.navdata.RunwayMapping;
import org.deltava.beans.schedule.Airport;

import org.deltava.comparators.AirportComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.JSONUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to update runway mappings.
 * @author Luke
 * @version 8.5
 * @since 8.3
 */

public class RunwayMappingCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving the Runway mapping.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {
		Airport a = SystemData.getAirport(ctx.getParameter("id"));
		Collection<RunwayMapping> maps = new ArrayList<RunwayMapping>();
		
		JSONObject jo = new JSONObject(ctx.getParameter("json"));
		JSONArray ma = jo.optJSONArray("mappings");
		for (int x = 0; (ma != null) && (x < ma.length()); x++) {
			JSONObject mo = ma.getJSONObject(x);
			RunwayMapping rm = new RunwayMapping(a.getICAO());
			rm.setOldCode(mo.getString("o"));
			rm.setNewCode(mo.getString("n"));
			maps.add(rm);
		}
		
		try {
			Connection con = ctx.getConnection();
			ctx.startTX();
			
			SetRunwayMapping rmwdao = new SetRunwayMapping(con);
			rmwdao.clear(a.getICAO());
			for (RunwayMapping rm : maps)
				rmwdao.write(rm);
			
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("rwymappings", null, a.getICAO());
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the Runway mapping.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		
		// Get the airport
		Airport a = SystemData.getAirport((String) ctx.getCmdParameter(ID, null));
		if (a == null)
			throw notFoundException("Invalid Airport - " + ctx.getParameter("airport"));
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the mappings
			GetRunwayMapping rmdao = new GetRunwayMapping(con);
			Collection<RunwayMapping> maps = rmdao.getAll(a);
			
			// Convert to JSON
			JSONObject jo = new JSONObject();
			jo.put("icao", a.getICAO());
			for (RunwayMapping rm : maps) {
				JSONObject rmo = new JSONObject();
				rmo.put("o", rm.getOldCode());
				rmo.put("n", rm.getNewCode());
				jo.append("mappings", rmo);
			}
			
			// Save the JSON
			JSONUtils.ensureArrayPresent(jo, "mappings");
			ctx.setAttribute("json", jo.toString(), REQUEST);
			
			// Load airport list
			SortedSet<Airport> airports = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME));
			airports.addAll(maps.isEmpty() ? SystemData.getAirports().values() : Collections.singleton(a));
			ctx.setAttribute("airports", airports, REQUEST);
			
			// Save in the request
			ctx.setAttribute("airport", a, REQUEST);
			ctx.setAttribute("rwyMappings", maps, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/navdata/rwyMapping.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the form.
	 * @param ctx the Command context
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}