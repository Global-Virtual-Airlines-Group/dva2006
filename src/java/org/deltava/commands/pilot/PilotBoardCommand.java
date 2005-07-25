// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.GeoLocation;

import org.deltava.beans.schedule.GeoPosition;
import org.deltava.beans.stats.PilotLocation;

import org.deltava.commands.*;

import org.deltava.dao.GetPilot;
import org.deltava.dao.DAOException;

/**
 * A Web Site Command to display Pilot locations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PilotBoardCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the Pilots and their locations
			GetPilot dao = new GetPilot(con);
			Map locations = dao.getPilotBoard();
			Map pilots = dao.getByID(locations.keySet(), "PILOTS");
			
			// Calculate the random location adjuster (between -1.5 and +1.5)
			Random rnd = new Random();
			double rndAmt = ((rnd.nextDouble() * 3) - 1) / GeoLocation.DEGREE_MILES;
			
			// Loop through the GeoLocations, apply the random adjuster and combine with the Pilot
			Map pilotLocations = new HashMap(pilots.size());
			for (Iterator i = pilots.keySet().iterator(); i.hasNext(); ) {
				Integer id = (Integer) i.next();
				GeoPosition gp = (GeoPosition) locations.get(id);
				gp.setLatitude(gp.getLatitude() + rndAmt);
				gp.setLongitude(gp.getLongitude() + rndAmt);
				
				// Create the pilot location
				Pilot usr = (Pilot) pilots.get(id);
				if (usr != null)
					pilotLocations.put(id, new PilotLocation(usr, gp));
			}
			
			// Save the locations
			ctx.setAttribute("locations", pilotLocations, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/pilotBoard.jsp");
		result.setSuccess(true);
	}
}