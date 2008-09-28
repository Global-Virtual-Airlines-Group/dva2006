// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.schedule.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

import org.gvagroup.common.*;

/**
 * A Web Site Command to modify Airport data.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class AirportCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving the Airport.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {

		// Get the command results
		CommandResult result = ctx.getResult();

		// Get the Airport code - if we're new, check if the airport exists
		String aCode = (String) ctx.getCmdParameter(ID, null);
		boolean isNew = (aCode == null);
		if (isNew) {
			Airport a = SystemData.getAirport(ctx.getParameter("iata"));
			if (a != null) {
				ctx.setMessage("Airport already exists - " + a.getName());

				// Save directions and time zones in request
				ctx.setAttribute("timeZones", TZInfo.getAll(), REQUEST);
				ctx.setAttribute("latDir", Arrays.asList(GeoLocation.LAT_DIRECTIONS), REQUEST);
				ctx.setAttribute("lonDir", Arrays.asList(GeoLocation.LON_DIRECTIONS), REQUEST);

				// Forward to the JSP
				result.setURL("/jsp/schedule/airportEdit.jsp");
				result.setSuccess(true);
				return;
			}
		}

		try {
			Airport a = null;
			Connection con = ctx.getConnection();

			// Get the DAO and the Airport
			if (!isNew) {
				GetAirport dao = new GetAirport(con);
				a = dao.get(aCode);
				if (a == null)
					throw notFoundException("Invalid Airport Code - " + aCode);

				// Load airport fields
				a.setName(ctx.getParameter("name"));
				a.setICAO(ctx.getParameter("icao"));
			} else
				a = new Airport(ctx.getParameter("iata"), ctx.getParameter("icao"), ctx.getParameter("name"));

			// Update the aiport from the request
			a.setTZ(ctx.getParameter("tz"));
			a.setAirlines(ctx.getParameters("airline"));

			// Build the airport latitude/longitude
			try {
				GeoPosition gp = new GeoPosition();
				int latD = Integer.parseInt(ctx.getParameter("latD"));
				int latM = Integer.parseInt(ctx.getParameter("latM"));
				int latS = Integer.parseInt(ctx.getParameter("latS"));
				gp.setLatitude(latD, latM, latS);

				// Convert to to southern hemisphere if necessary
				if (StringUtils.arrayIndexOf(GeoLocation.LAT_DIRECTIONS, ctx.getParameter("latDir")) == 1)
					gp.setLatitude(gp.getLatitude() * -1);

				// Parse longitude
				int lonD = Integer.parseInt(ctx.getParameter("lonD"));
				int lonM = Integer.parseInt(ctx.getParameter("lonM"));
				int lonS = Integer.parseInt(ctx.getParameter("lonS"));
				gp.setLongitude(lonD, lonM, lonS);
				
				// Convert to western hemisphere if necessary
				if (StringUtils.arrayIndexOf(GeoLocation.LON_DIRECTIONS, ctx.getParameter("lonDir")) == 1)
					gp.setLongitude(gp.getLongitude() * -1);

				// Update the airport
				a.setLocation(gp.getLatitude(), gp.getLongitude());
			} catch (NumberFormatException nfe) {
				throw new CommandException("Error parsing Airport latitude/longitude", false);
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

		// Update the Airports
		EventDispatcher.send(SystemEvent.AirportReload());

		// Set status update flag
		ctx.setAttribute("isAirport", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		result.setType(ResultType.REQREDIRECT);
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
		String aCode = (String) ctx.getCmdParameter(ID, null);
		boolean isNew = (aCode == null);

		// Validate the airport if we are not creating a new one
		if (!isNew) {
			Airport a = null;
			try {
				Connection con = ctx.getConnection();

				// Get the DAO and the Airport
				GetAirport dao = new GetAirport(con);
				a = dao.get(aCode);
				if (a == null) {
					String icao = dao.getICAO(aCode);
					if (icao != null) {
						GetNavData nvdao = new GetNavData(con);
						GeospaceLocation al = nvdao.getAirport(icao);
						if (al != null) {
							a = new Airport((aCode.length() == 3) ? aCode : "", icao, "");
							a.setLocation(al.getLatitude(), al.getLongitude());
							a.setAltitude(al.getAltitude());
						}
					}

					ctx.setAttribute("isNew", Boolean.TRUE, REQUEST);
				}
			} catch (DAOException de) {
				throw new CommandException(de);
			} finally {
				ctx.release();
			}

			// If we have an airport, save it in the request
			if (a != null) {
				ctx.setAttribute("airport", a, REQUEST);

				// Convert the geoPosition into degrees, minutes, seconds
				GeoPosition gp = a.getPosition();
				int latS = new Double(GeoPosition.getSeconds(gp.getLatitude())).intValue();
				int lngS = new Double(GeoPosition.getSeconds(gp.getLongitude())).intValue();
				
				ctx.setAttribute("latD", Integer.valueOf(Math.abs(GeoPosition.getDegrees(gp.getLatitude()))), REQUEST);
				ctx.setAttribute("latM", Integer.valueOf(GeoPosition.getMinutes(gp.getLatitude())), REQUEST);
				ctx.setAttribute("latS", Integer.valueOf(latS), REQUEST);
				ctx.setAttribute("latNS", GeoLocation.LAT_DIRECTIONS[((gp.getLatitude() < 0) ? 1 : 0)], REQUEST);
				ctx.setAttribute("lonD", Integer.valueOf(Math.abs(GeoPosition.getDegrees(gp.getLongitude()))), REQUEST);
				ctx.setAttribute("lonM", Integer.valueOf(GeoPosition.getMinutes(gp.getLongitude())), REQUEST);
				ctx.setAttribute("lonS", Integer.valueOf(lngS), REQUEST);
				ctx.setAttribute("lonEW", GeoLocation.LON_DIRECTIONS[((gp.getLongitude() < 0) ? 1 : 0)], REQUEST);
			}
		}

		// Save directions and time zones in request
		ctx.setAttribute("timeZones", TZInfo.getAll(), REQUEST);
		ctx.setAttribute("latDir", Arrays.asList(GeoLocation.LAT_DIRECTIONS), REQUEST);
		ctx.setAttribute("lonDir", Arrays.asList(GeoLocation.LON_DIRECTIONS), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/airportEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the Airport. <i>NOT IMPLEMENTED - Edits the Airport</i>
	 * @param ctx the Command context
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}