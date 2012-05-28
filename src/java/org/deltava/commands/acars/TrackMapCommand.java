// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.commands.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display an ACARS track map. 
 * @author Luke
 * @version 4.2
 * @since 4.2
 */

public class TrackMapCommand extends AbstractCommand {

	private static final GeoLocation DEFAULT_CTR = 
			new GeoPosition(SystemData.getDouble("acars.livemap.lat", 40), SystemData.getDouble("acars.livemap.lng", -85));
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Create the map center
		GeoPosition ctr = new GeoPosition(StringUtils.parse(ctx.getParameter("lat"), DEFAULT_CTR.getLatitude()), 
				StringUtils.parse(ctx.getParameter("lng"), DEFAULT_CTR.getLongitude()));
		try {
			if (ctx.getParameter("lat") == null) {
				String lat = ctx.getCookie("acarsMapLat").getValue();
				String lng = ctx.getCookie("acarsMapLng").getValue();
				ctr = new GeoPosition(StringUtils.parse(lat, DEFAULT_CTR.getLatitude()), StringUtils.parse(lng, DEFAULT_CTR.getLongitude()));
			}
		} catch (Exception e) {
			ctr = new GeoPosition(DEFAULT_CTR);
		}
		
		// Save the map center
		ctx.setAttribute("mapCenter", ctr, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/acars/trackMap.jsp");
		result.setSuccess(true);
	}
}