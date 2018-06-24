// Copyright 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.navdata.RunwayMapping;
import org.deltava.beans.schedule.Airport;

import org.deltava.comparators.AirportComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to update runway mappings.
 * @author Luke
 * @version 8.3
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
		Airport a = SystemData.getAirport(ctx.getParameter("icao")); 
		boolean isDelete = Boolean.valueOf(ctx.getParameter("isDelete")).booleanValue();
		try {
			Connection con = ctx.getConnection();
			SetRunwayMapping rmwdao = new SetRunwayMapping(con);
			
			// Get the mapping
			if (isDelete) {
				String oldCode = String.valueOf(ctx.getCmdParameter(ID, null));
				GetRunwayMapping rmdao = new GetRunwayMapping(con);
				RunwayMapping rm = rmdao.get(a, oldCode);
				if (rm == null)
					throw notFoundException("Invalid Runway mapping - " + a.getICAO() + " " + oldCode);
				
				rmwdao.delete(rm);
			} else {
				RunwayMapping rm = new RunwayMapping(a.getICAO());
				rm.setOldCode(ctx.getParameter("oldCode"));
				rm.setNewCode(ctx.getParameter("newCode"));
				rmwdao.write(rm);
			}
		} catch (DAOException de) {
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
		Airport a = SystemData.getAirport(ctx.getParameter("airport"));
		if (a == null)
			throw notFoundException("Invalid Airport - " + ctx.getParameter("airport"));
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the mapping
			String oldCode = String.valueOf(ctx.getCmdParameter(ID, null));
			GetRunwayMapping rmdao = new GetRunwayMapping(con);
			RunwayMapping rm = rmdao.get(a, oldCode);
			
			// Load airport list
			SortedSet<Airport> airports = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME));
			airports.addAll((rm == null) ? SystemData.getAirports().values() : Collections.singleton(SystemData.getAirport(rm.getICAO())));
			ctx.setAttribute("airports", airports, REQUEST);
			
			// Save in the request
			ctx.setAttribute("airport", a, REQUEST);
			ctx.setAttribute("rmap", rm, REQUEST);
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