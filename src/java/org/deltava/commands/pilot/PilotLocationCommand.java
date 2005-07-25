 // Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.Arrays;
import java.sql.Connection;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.Pilot;
import org.deltava.beans.schedule.GeoPosition;
import org.deltava.beans.stats.PilotLocation;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.StringUtils;

/**
 * A Web Site Command to set a user's geolocation.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PilotLocationCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command result
		CommandResult result = ctx.getResult();

		try {
			Connection con = ctx.getConnection();

			// Get the pilot's location
			GetPilot dao = new GetPilot(con);
			GeoLocation gp = dao.getLocation(ctx.getUser().getID());
			if (gp == null)
				gp = new GeoPosition(38.88, -93.25);
			
			// If we have a lat/lon pair, then update the location
			ctx.setAttribute("mapCenter", gp, REQUEST);
			ctx.setAttribute("location", new PilotLocation((Pilot) ctx.getUser(), gp), REQUEST);
			if (ctx.getParameter("latD") != null) {
				// Build the pilot latitude/longitude
				GeoPosition loc = null;
				try {
					int latD = Integer.parseInt(ctx.getParameter("latD"));
					int latM = Integer.parseInt(ctx.getParameter("latM"));
					int latS = Integer.parseInt(ctx.getParameter("latS"));
					latD *= (StringUtils.arrayIndexOf(GeoLocation.LAT_DIRECTIONS, ctx.getParameter("latDir")) * -1);

					int lonD = Integer.parseInt(ctx.getParameter("lonD"));
					int lonM = Integer.parseInt(ctx.getParameter("lonM"));
					int lonS = Integer.parseInt(ctx.getParameter("lonS"));
					lonD *= (StringUtils.arrayIndexOf(GeoLocation.LON_DIRECTIONS, ctx.getParameter("lonDir")) * -1);

					// Build the GeoPosition bean and update the airport
					loc = new GeoPosition();
					loc.setLatitude(latD, latM, latS);
					loc.setLongitude(lonD, lonM, lonS);
				} catch (NumberFormatException nfe) {
					throw new CommandException("Error parsing Pilot latitude/longitude");
				}
				
				// Update the pilot location
				SetPilot wdao = new SetPilot(con);
				wdao.setLocation(ctx.getUser().getID(), loc);
				
				// Forward to the JSP
				ctx.setAttribute("location", gp, REQUEST);
				result.setURL("/jsp/pilot/geoLocateUpdate.jsp");
			} else {
				// Convert the geoPosition into degrees, minutes, seconds
				ctx.setAttribute("latD", new Integer(Math.abs(GeoPosition.getDegrees(gp.getLatitude()))), REQUEST);
				ctx.setAttribute("latM", new Integer(GeoPosition.getMinutes(gp.getLatitude())), REQUEST);
				ctx.setAttribute("latS", new Integer(GeoPosition.getSeconds(gp.getLatitude())), REQUEST);
				ctx.setAttribute("latNS", GeoLocation.LAT_DIRECTIONS[((gp.getLatitude() < 0) ? 1 : 0)], REQUEST);
				ctx.setAttribute("lonD", new Integer(Math.abs(GeoPosition.getDegrees(gp.getLongitude()))), REQUEST);
				ctx.setAttribute("lonM", new Integer(GeoPosition.getMinutes(gp.getLongitude())), REQUEST);
				ctx.setAttribute("lonS", new Integer(GeoPosition.getSeconds(gp.getLongitude())), REQUEST);
				ctx.setAttribute("lonEW", GeoLocation.LON_DIRECTIONS[((gp.getLongitude() < 0) ? 1 : 0)], REQUEST);
				
				// Save the direction names
				ctx.setAttribute("latDir", Arrays.asList(GeoLocation.LAT_DIRECTIONS), REQUEST);
				ctx.setAttribute("lonDir", Arrays.asList(GeoLocation.LON_DIRECTIONS), REQUEST);

				// Forward to the JSP
				result.setURL("/jsp/pilot/geoLocate.jsp");
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Save the pilot bean
		ctx.setAttribute("pilot", ctx.getUser(), REQUEST);
		
		// Forward to the JSP
		result.setSuccess(true);
	}
}