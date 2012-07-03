// Copyright 2005, 2006, 2007, 2009, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to view all collected ACARS information about a flight.
 * @author Luke
 * @version 4.2
 * @since 1.0
 */

public class FlightInfoCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();

			// Get the DAO and Flight Information
			GetACARSPositions dao = new GetACARSPositions(con);
			FlightInfo info = dao.getInfo(ctx.getID());
			if (info == null)
				throw notFoundException("Invalid ACARS Flight ID - " + ctx.getID());

			// Get the user location and database
			GetUserData uddao = new GetUserData(con);
			UserData uloc = uddao.get(info.getPilotID());
			if (uloc == null)
				throw notFoundException("Invalid Pilot ID - " + info.getPilotID());

			// Get the PIREP itself (this too might be null, but one or the other won't be)
			GetFlightReports prdao = new GetFlightReports(con);
			FDRFlightReport afr = prdao.getACARS(uloc.getDB(), info.getID());

			// Load the Pilot
			GetPilot pdao = new GetPilot(con);
			Pilot usr = pdao.get(uloc);
			
			// Load the dispatcher
			if (info.getDispatcherID() != 0) {
				UserData dud = uddao.get(info.getDispatcherID());
				ctx.setAttribute("dispatcher", pdao.get(dud), REQUEST);
			}
			
			// Load the route
			if (info.getRouteID() != 0) {
				GetACARSRoute ardao = new GetACARSRoute(con);
				ctx.setAttribute("route", ardao.getRoute(info.getRouteID()), REQUEST);
			}

			// Save the data we have loaded
			ctx.setAttribute("pilot", usr, REQUEST);
			ctx.setAttribute("pirep", afr, REQUEST);
			ctx.setAttribute("info", info, REQUEST);

			// Get the route data from the DAFIF database
			List<String> routeEntries = StringUtils.split(info.getRoute(), " ");
			GeoPosition lastWaypoint = new GeoPosition(info.getAirportD());
			int distance = info.getAirportD().getPosition().distanceTo(info.getAirportA());
			
			// Get navigation aids
			GetNavData navdao = new GetNavData(con);
			NavigationDataMap navaids = navdao.getByID(routeEntries);
			
			// Filter out navaids and put them in the correct order
			List<NavigationDataBean> routeInfo = new ArrayList<NavigationDataBean>();
			for (Iterator<String> i = routeEntries.iterator(); i.hasNext();) {
				String navCode = i.next();
				NavigationDataBean wPoint = navaids.get(navCode, lastWaypoint);
				if (wPoint != null) {
					if (lastWaypoint.distanceTo(wPoint) < distance) {
						routeInfo.add(wPoint);
						lastWaypoint.setLatitude(wPoint.getLatitude());
						lastWaypoint.setLongitude(wPoint.getLongitude());
					}
				}
			}

			// Load the route data
			List<GeoLocation> positions = dao.getRouteEntries(info.getID(), false, info.getArchived());

			// Calculate and save the map center for the Google Map
			if (!positions.isEmpty()) {
			   GeoPosition start = new GeoPosition(positions.get(0));
			   GeoLocation end = positions.get(positions.size() - 1);
			   ctx.setAttribute("mapCenter", start.midPoint(end), REQUEST);
			   ctx.setAttribute("routeLength", new Integer(start.distanceTo(end)), REQUEST);
			} else {
			   GeoPosition start = info.getAirportD().getPosition();
			   ctx.setAttribute("mapCenter", start.midPoint(info.getAirportA()), REQUEST);
			   ctx.setAttribute("routeLength", new Integer(start.distanceTo(info.getAirportA())), REQUEST);
			}
			
			// Save the filed/actual routes
			ctx.setAttribute("filedRoute", routeInfo, REQUEST);
			ctx.setAttribute("mapRoute", positions, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/acars/flightInfo.jsp");
		result.setSuccess(true);
	}
}