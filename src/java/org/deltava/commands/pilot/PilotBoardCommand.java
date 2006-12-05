// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 1.0
 * @since 1.0
 */

public class PilotBoardCommand extends AbstractCommand {
   
   private class SpecialLocation implements MapEntry {
      
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
		try {
			Connection con = ctx.getConnection();
			
			// Get the active equipment types
			GetEquipmentType eqdao = new GetEquipmentType(con);
			ctx.setAttribute("eqTypes", eqdao.getActive(), REQUEST);
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