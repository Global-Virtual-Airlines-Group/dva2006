// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.TZInfo;
import org.deltava.beans.GeoLocation;

import org.deltava.beans.schedule.*;
import org.deltava.commands.*;

import org.deltava.dao.GetAirport;
import org.deltava.dao.SetSchedule;
import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to modify Airport data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AirportCommand extends AbstractFormCommand {
   
	/**
	 * Callback method called when saving the Airport.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {

		// Get the Airport code
		String aCode = (String) ctx.getCmdParameter(Command.ID, null);
		boolean isNew = (aCode == null);

		Airport a = null;
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the Airport
			if (!isNew) {
				GetAirport dao = new GetAirport(con);
				a = dao.get(aCode);
				if (a == null)
					throw new CommandException("Invalid Airport Code - " + aCode);
				
				// Load airport fields
				a.setName(ctx.getParameter("name"));
				a.setICAO(ctx.getParameter("icao"));
			} else {
				a = new Airport(ctx.getParameter("iata"), ctx.getParameter("icao"), ctx.getParameter("name"));
			}
			
			// Update the aiport from the request
			a.setTZ(ctx.getParameter("tz"));
			a.setAirlines(Arrays.asList(ctx.getRequest().getParameterValues("airline")));
			
			// Build the airport latitude/longitude
			try {
				int latD = Integer.parseInt(ctx.getParameter("latD"));
				int latM = Integer.parseInt(ctx.getParameter("latM"));
				int latS = Integer.parseInt(ctx.getParameter("latS"));
				if (StringUtils.arrayIndexOf(GeoLocation.LAT_DIRECTIONS, ctx.getParameter("latDir")) == 1)
					latD *=  -1;
				
				int lonD = Integer.parseInt(ctx.getParameter("lonD"));
				int lonM = Integer.parseInt(ctx.getParameter("lonM"));
				int lonS = Integer.parseInt(ctx.getParameter("lonS"));
				if (StringUtils.arrayIndexOf(GeoLocation.LON_DIRECTIONS, ctx.getParameter("lonDir")) == 1)
					lonD *=  -1;

				// Build the GeoPosition bean and update the airport
				GeoPosition gp = new GeoPosition();
				gp.setLatitude(latD, latM, latS);
				gp.setLongitude(lonD, lonM, lonS);
				a.setLocation(gp.getLatitude(), gp.getLongitude());
			} catch (NumberFormatException nfe) {
				throw new CommandException("Error parsing Airport latitude/longitude");
			}
			
			// Get the DAO and write the airport
			SetSchedule wdao = new SetSchedule(con);
			if (isNew) {
				wdao.create(a);
				ctx.setAttribute("isCreate", Boolean.TRUE, REQUEST);
			} else {
				wdao.update(a);
				ctx.setAttribute("isUpdate", Boolean.TRUE, REQUEST);
			}
			
			// Save the airport in the request
			ctx.setAttribute("airport", a, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Update the SystemData map
		Map airports = (Map) SystemData.getObject("airports");
		airports.put(a.getIATA(), a);
		airports.put(a.getICAO(), a);
		
		// Set status update flag
		ctx.setAttribute("isAirport", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/schedule/scheduleUpdate.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the Airport.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {

		// Get the Airport code
		String aCode = (String) ctx.getCmdParameter(Command.ID, null);
		boolean isNew = (aCode == null);
		
		// Save directions and time zones in request
		ctx.setAttribute("timeZones", TZInfo.getAll(), REQUEST);
		ctx.setAttribute("latDir", Arrays.asList(GeoLocation.LAT_DIRECTIONS), REQUEST);
		ctx.setAttribute("lonDir", Arrays.asList(GeoLocation.LON_DIRECTIONS), REQUEST);

		// Validate the airport if we are not creating a new one
		if (!isNew) {
			try {
				Connection con = ctx.getConnection();

				// Get the DAO and the Airport
				GetAirport dao = new GetAirport(con);
				Airport a = dao.get(aCode);
				if (a == null)
					throw new CommandException("Invalid Airport Code - " + aCode);

				// Save the airport in the request
				ctx.setAttribute("airport", a, REQUEST);
				
				// Convert the geoPosition into degrees, minutes, seconds
				GeoPosition gp = a.getPosition();
				ctx.setAttribute("latD", new Integer(Math.abs(GeoPosition.getDegrees(gp.getLatitude()))), REQUEST);
				ctx.setAttribute("latM", new Integer(GeoPosition.getMinutes(gp.getLatitude())), REQUEST);
				ctx.setAttribute("latS", new Integer(GeoPosition.getSeconds(gp.getLatitude())), REQUEST);
				ctx.setAttribute("latNS", GeoLocation.LAT_DIRECTIONS[((gp.getLatitude() < 0) ? 1 : 0)], REQUEST);
				ctx.setAttribute("lonD", new Integer(Math.abs(GeoPosition.getDegrees(gp.getLongitude()))), REQUEST);
				ctx.setAttribute("lonM", new Integer(GeoPosition.getMinutes(gp.getLongitude())), REQUEST);
				ctx.setAttribute("lonS", new Integer(GeoPosition.getSeconds(gp.getLongitude())), REQUEST);
				ctx.setAttribute("lonEW", GeoLocation.LON_DIRECTIONS[((gp.getLongitude() < 0) ? 1 : 0)], REQUEST);
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/airportEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the Airport. <i>NOT IMPLEMENTED </i>
	 * @param ctx the Command context
	 * @throws UnsupportedOperationException always
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		throw new UnsupportedOperationException();
	}
}