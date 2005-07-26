// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.acars;

import javax.servlet.http.*;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.commands.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display a live ACARS Map.
 * @author Luke
 * @version 1.0
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
      String lat = getCookie(ctx.getRequest(), "acars_map_lat");
      String lng = getCookie(ctx.getRequest(), "acars_map_lng");
      
      // Create the map center
      GeoPosition gp = null;
      try {
         gp = new GeoPosition(Double.parseDouble(lat), Double.parseDouble(lng));
      } catch (Exception e) {
         gp = new GeoPosition(SystemData.getDouble("acars.map.lat"), SystemData.getDouble("acars.map.lng"));
      }
      
      // Save the map center and refresh interval
      ctx.setAttribute("mapCenter", gp, REQUEST);
      ctx.setAttribute("refresh", new Integer(SystemData.getInt("acars.map.refresh")), REQUEST);
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/acars/acarsMap.jsp");
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