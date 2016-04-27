// Copyright 2005, 2006, 2007, 2008, 2009, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.List;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.commands.*;

import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display Pilot locations.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class PilotBoardCommand extends AbstractCommand {
	
	private static final String[] MT_NAMES = {"Pilot Locations", "Heat Map"};
	private static final String[] MT_OPTS = {"LOC", "MAP"};
	private static final List<?> MAP_TYPES = ComboUtils.fromArray(MT_NAMES, MT_OPTS);

	private class SpecialLocation implements MarkerMapEntry {

		private final GeoPosition _gPos;

		SpecialLocation(double lat, double lon) {
			super();
			_gPos = new GeoPosition(lat, lon);
		}

		@Override
		public double getLatitude() {
			return _gPos.getLatitude();
		}

		@Override
		public double getLongitude() {
			return _gPos.getLongitude();
		}

		@Override
		public String getIconColor() {
			return MapEntry.RED;
		}

		@Override
		public String getInfoBox() {
			StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox\"><span class=\"bld\">");
			buf.append(SystemData.get("airline.name"));
			buf.append("</span><br /><br />Position: ");
			buf.append(StringUtils.format(_gPos, true, GeoLocation.ALL));
			buf.append("<br /><a href=\"http");
			if (SystemData.getBoolean("security.ssl"))
				buf.append('s');
			
			buf.append("://");
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
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the HQ
		GeoLocation hq = new SpecialLocation(SystemData.getDouble("airline.location.lat", 40), SystemData.getDouble("airline.location.lng", -85));
		ctx.setAttribute("hq", hq, REQUEST);

		// Find my location
		ctx.setAttribute("mapOptions", MAP_TYPES, REQUEST);
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