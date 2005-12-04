// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.GeoLocation;
import org.deltava.beans.MapEntry;

import org.deltava.beans.schedule.GeoPosition;
import org.deltava.beans.stats.PilotLocation;

import org.deltava.commands.*;

import org.deltava.dao.GetPilot;
import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display Pilot locations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PilotBoardCommand extends AbstractCommand {
   
   private class SpecialLocation implements GeoLocation, MapEntry {
      
      private GeoPosition _gPos;
      
      public SpecialLocation(double lat, double lon) {
         super();
         _gPos = new GeoPosition(lat, lon);
      }
      
      public double getLatitude() {
         return _gPos.getLatitude();
      }
      
      public double getLongitude() {
         return _gPos.getLongitude();
      }
      
      public int getHemisphere() {
         return _gPos.getHemisphere();
      }
      
      public String getIconColor() {
         return MapEntry.RED;
      }
      
      public String getInfoBox() {
         StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox\"><span class=\"small bld\">");
         buf.append(SystemData.get("airline.name"));
         buf.append("</span><span class=\"small\"><br /><br />Position: ");
         buf.append(StringUtils.format(_gPos, true, GeoLocation.ALL));
         buf.append("<br /><a href=\"http://");
         buf.append(SystemData.get("airline.url"));
         buf.append("/\">http://");
         buf.append(SystemData.get("airline.url"));
         buf.append("/</a></span></div>");
         return buf.toString();
      }
   }

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
			Map<Integer, GeoLocation> locations = dao.getPilotBoard();
			Map<Integer, Pilot> pilots = dao.getByID(locations.keySet(), "PILOTS");
			
			// Calculate the random location adjuster (between -1.5 and +1.5)
			Random rnd = new Random();
			double rndAmt = ((rnd.nextDouble() * 3) - 1) / GeoLocation.DEGREE_MILES;
			
			// Loop through the GeoLocations, apply the random adjuster and combine with the Pilot
			Set<PilotLocation> pilotLocations = new HashSet<PilotLocation>(pilots.size());
			for (Iterator<Integer> i = pilots.keySet().iterator(); i.hasNext(); ) {
				Integer id = i.next();
				GeoPosition gp = new GeoPosition(locations.get(id));
				gp.setLatitude(gp.getLatitude() + rndAmt);
				gp.setLongitude(gp.getLongitude() + rndAmt);
				
				// Create the pilot location
				Pilot usr = pilots.get(id);
				if (usr != null)
					pilotLocations.add(new PilotLocation(usr, gp));
			}
			
			// Save the locations
			ctx.setAttribute("locations", pilotLocations, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Add the home base
		ctx.setAttribute("mapCenter", new SpecialLocation(SystemData.getDouble("airline.location.lat", 40), 
		      SystemData.getDouble("airline.location.lng", -85)), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/pilotMap.jsp");
		result.setSuccess(true);
	}
}