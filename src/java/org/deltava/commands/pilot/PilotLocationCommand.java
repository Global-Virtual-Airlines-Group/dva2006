// Copyright 2005, 2006, 2008, 2009, 2010, 2011, 2012, 2016, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.sql.Connection;

import org.apache.logging.log4j.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.GeoPosition;
import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.http.GetGoogleGeocode;

import org.deltava.util.*;

/**
 * A Web Site Command to set a user's geolocation.
 * @author Luke
 * @version 11.0
 * @since 1.0
 */

public class PilotLocationCommand extends AbstractCommand {

	private static final Logger log = LogManager.getLogger(PilotLocationCommand.class);
	
	private static final GeoLocation DEFAULT = new GeoPosition(38.88, -93.25);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command result and the user
		CommandResult result = ctx.getResult();
		Pilot p = ctx.getUser();
		
		// Get the IP address Info
		GeoLocation addrInfo = (GeoLocation) ctx.getSession().getAttribute(HTTPContext.ADDRINFO_ATTR_NAME);

		// Check if we are deleting the profile
		boolean isDelete = "delete".equals(ctx.getCmdParameter(OPERATION, null));
		try {
			Connection con = ctx.getConnection();
			
			// Get the pilot's location
			GetPilotBoard dao = new GetPilotBoard(con);
			GeoLocation gp = dao.getLocation(ctx.getUser().getID());
			if ((gp == null) && (addrInfo != null)) {
				ctx.setAttribute("mapCenter", addrInfo, REQUEST);
				ctx.setAttribute("location", new PilotLocation(p, addrInfo), REQUEST);
			} else if (gp == null) {
				GetIPLocation ipdao = new GetIPLocation(con);
				GeoLocation loc = ipdao.get(ctx.getRequest().getRemoteAddr());
				ctx.setAttribute("mapCenter", (loc == null) ? DEFAULT : loc, REQUEST);
			} else {
				ctx.setAttribute("location", new PilotLocation(p, gp), REQUEST);
				ctx.setAttribute("mapCenter", gp, REQUEST);
			}

			// If we have a lat/lon pair, then update the location
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
				GeocodeResult geoCode = null;
				if (GeoUtils.isValid(loc)) {
					try {
						GetGoogleGeocode gcdao = new GetGoogleGeocode();
						geoCode = gcdao.getGeoData(loc);
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}

					// Start a transaction
					ctx.startTX();

					// Update the map location
					SetPilot wdao = new SetPilot(con);
					wdao.setLocation(ctx.getUser().getID(), loc);

					// Update the home town
					if (geoCode != null) {
						wdao.setHomeTown(ctx.getUser().getID(), geoCode);
						ctx.setAttribute("geoCode", geoCode, REQUEST);
					}

					// Commit
					ctx.commitTX();
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
				if (gp != null) {
					int latS = (int) GeoPosition.getSeconds(gp.getLatitude());
					int lngS = (int) GeoPosition.getSeconds(gp.getLongitude());
					ctx.setAttribute("latD", Integer.valueOf(Math.abs(GeoPosition.getDegrees(gp.getLatitude()))), REQUEST);
					ctx.setAttribute("latM", Integer.valueOf(GeoPosition.getMinutes(gp.getLatitude())), REQUEST);
					ctx.setAttribute("latS", Integer.valueOf(latS), REQUEST);
					ctx.setAttribute("latNS", GeoLocation.LAT_DIRECTIONS[((gp.getLatitude() < 0) ? 1 : 0)], REQUEST);
					ctx.setAttribute("lonD", Integer.valueOf(Math.abs(GeoPosition.getDegrees(gp.getLongitude()))), REQUEST);
					ctx.setAttribute("lonM", Integer.valueOf(GeoPosition.getMinutes(gp.getLongitude())), REQUEST);
					ctx.setAttribute("lonS", Integer.valueOf(lngS), REQUEST);
					ctx.setAttribute("lonEW", GeoLocation.LON_DIRECTIONS[((gp.getLongitude() < 0) ? 1 : 0)], REQUEST);
				}

				// Save the direction names
				ctx.setAttribute("latDir", Arrays.asList(GeoLocation.LAT_DIRECTIONS), REQUEST);
				ctx.setAttribute("lonDir", Arrays.asList(GeoLocation.LON_DIRECTIONS), REQUEST);

				// Forward to the JSP
				result.setURL("/jsp/pilot/geoLocate.jsp");
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
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