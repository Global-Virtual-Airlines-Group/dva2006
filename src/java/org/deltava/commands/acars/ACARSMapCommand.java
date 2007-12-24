// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.util.*;

import javax.servlet.http.*;

import org.deltava.beans.schedule.GeoPosition;

import org.deltava.commands.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display a live ACARS Map.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class ACARSMapCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Check if we have a map center cookie set
		String lat = getCookie(ctx.getRequest(), "acarsMapLat");
		String lng = getCookie(ctx.getRequest(), "acarsMapLng");
		
		// Calcualte the settings cookie expiry date
		Date expiryDate = CalendarUtils.adjust(new Date(), 180);
		ctx.setAttribute("cookieExpiry", StringUtils.format(expiryDate, "yyyy, MM, dd"), REQUEST);

		// Create the map center
		GeoPosition gp = null;
		try {
			gp = new GeoPosition(Double.parseDouble(lat), Double.parseDouble(lng));
		} catch (Exception e) {
			gp = new GeoPosition(SystemData.getDouble("acars.livemap.lat", 40), SystemData.getDouble("acars.livemap.lng", -85));
		}

		// Save the map center
		ctx.setAttribute("mapCenter", gp, REQUEST);

		// Set the cache
		ctx.getCache().setMaxAge(120);
		
		// Check if we're retrieving this from the ACARS client
		boolean isACARSClient = Boolean.valueOf(ctx.getParameter("acarsClient")).booleanValue();
		ctx.setAttribute("isDispatch", Boolean.valueOf(ctx.getParameter("dispatchClient")), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL(isACARSClient ? "/jsp/acars/acarsClientMap.jsp" : "/jsp/acars/acarsMap.jsp");
		result.setSuccess(true);
	}

	/**
	 * Helper method to return the value of a particular cookie.
	 */
	private String getCookie(HttpServletRequest req, String name) {
		Cookie[] cookies = req.getCookies();
		if (cookies == null)
			return null;

		for (int x = 0; x < cookies.length; x++) {
			Cookie c = cookies[x];
			if (c.getName().equals(name))
				return c.getValue();
		}

		return null;
	}
}