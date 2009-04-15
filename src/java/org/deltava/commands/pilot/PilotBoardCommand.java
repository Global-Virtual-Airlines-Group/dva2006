// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.commands.*;

import org.deltava.dao.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display Pilot locations.
 * @author Luke
 * @version 2.5
 * @since 1.0
 */

public class PilotBoardCommand extends AbstractCommand {

	private class SpecialLocation implements MarkerMapEntry {

		private GeoPosition _gPos;

		SpecialLocation(double lat, double lon) {
			super();
			_gPos = new GeoPosition(lat, lon);
		}

		public double getLatitude() {
			return _gPos.getLatitude();
		}

		public double getLongitude() {
			return _gPos.getLongitude();
		}

		public String getIconColor() {
			return MapEntry.RED;
		}

		public String getInfoBox() {
			StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox\"><span class=\"bld\">");
			buf.append(SystemData.get("airline.name"));
			buf.append("</span><br /><br />Position: ");
			buf.append(StringUtils.format(_gPos, true, GeoLocation.ALL));
			buf.append("<br /><a href=\"http://");
			buf.append(SystemData.get("airline.url"));
			buf.append("/\">http://");
			buf.append(SystemData.get("airline.url"));
			buf.append("/</a></div>");
			return buf.toString();
		}
	}

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the HQ
		GeoLocation hq = new SpecialLocation(SystemData.getDouble("airline.location.lat", 40), SystemData.getDouble("airline.location.lng", -85));
		ctx.setAttribute("hq", hq, REQUEST);

		// Find my location
		try {
			Connection con = ctx.getConnection();

			// Get the pilot's location
			GeoLocation gp = hq;
			if (ctx.isAuthenticated()) {
				GetPilotBoard dao = new GetPilotBoard(con);
				gp = dao.getLocation(ctx.getUser().getID());
				if (gp == null)
					gp = (GeoLocation) ctx.getSession().getAttribute(HTTPContext.ADDRINFO_ATTR_NAME);
				if (gp == null)
					gp = hq;
			}
			
			// Load the equipment types
			GetEquipmentType eqdao = new GetEquipmentType(con);
			ctx.setAttribute("eqTypes", eqdao.getActive(), REQUEST);

			// Save my location
			ctx.setAttribute("mapCenter", gp, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/pilotMap.jsp");
		result.setSuccess(true);
	}
}