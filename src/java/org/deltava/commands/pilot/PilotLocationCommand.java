// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.Arrays;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.schedule.GeoPosition;
import org.deltava.beans.stats.PilotLocation;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to set a user's geolocation.
 * @author Luke
 * @version 2.3
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
		
		// Check if we are deleting the profile
		boolean isDelete = "delete".equals(ctx.getCmdParameter(OPERATION, null));
		try {
			Connection con = ctx.getConnection();

			// Get the pilot's location
			GetPilot dao = new GetPilot(con);
			GeoLocation gp = dao.getLocation(ctx.getUser().getID());
			if (gp == null)
				gp = new GeoPosition(38.88, -93.25);
			else {
			   ctx.setAttribute("location", new PilotLocation((Pilot) ctx.getUser(), gp), REQUEST);   
			   if (gp instanceof MapEntry)
			      ctx.setAttribute("locationText", StringUtils.escapeSlashes(((MapEntry) gp).getInfoBox()), REQUEST);
			}
			
			// If we have a lat/lon pair, then update the location
			ctx.setAttribute("mapCenter", gp, REQUEST);
			if (ctx.getParameter("latD") != null) {
				// Build the pilot latitude/longitude
				GeoPosition loc = new GeoPosition();
				int latD = StringUtils.parse(ctx.getParameter("latD"), 0);
				int latM = StringUtils.parse(ctx.getParameter("latM"), 0);
				int latS = StringUtils.parse(ctx.getParameter("latS"), 0);
				loc.setLatitude(latD, latM, latS);
				if (StringUtils.arrayIndexOf(GeoLocation.LAT_DIRECTIONS, ctx.getParameter("latDir")) == 1)
					loc.setLatitude(loc.getLatitude() * -1);

				// Update the longitude
				int lonD = StringUtils.parse(ctx.getParameter("lonD"), 0);
				int lonM = StringUtils.parse(ctx.getParameter("lonM"), 0);
				int lonS = StringUtils.parse(ctx.getParameter("lonS"), 0);
				loc.setLongitude(lonD, lonM, lonS);
				if (StringUtils.arrayIndexOf(GeoLocation.LON_DIRECTIONS, ctx.getParameter("lonDir")) == 1)
					loc.setLongitude(loc.getLongitude() * -1);
				
				// Update the pilot location
				if ((loc.getLatitude() != 0.0) && (loc.getLongitude() != 0.0)) {
					SetPilot wdao = new SetPilot(con);
					wdao.setLocation(ctx.getUser().getID(), loc);
				}
				
				// Forward to the JSP
				ctx.setAttribute("location", gp, REQUEST);
				result.setURL("/jsp/pilot/geoLocateUpdate.jsp");
			} else if (isDelete) {
			   SetPilot wdao = new SetPilot(con);
			   wdao.clearLocation(ctx.getUser().getID());
			   
			   // Forward to the JSP
			   ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);
			   result.setURL("/jsp/pilot/geoLocateUpdate.jsp");
			} else {
				// Convert the geoPosition into degrees, minutes, seconds
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