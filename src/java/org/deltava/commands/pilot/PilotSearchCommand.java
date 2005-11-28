// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;

import org.deltava.commands.*;

import org.deltava.dao.GetPilot;
import org.deltava.dao.DAOException;

import org.deltava.security.command.PilotAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to search for Pilots.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PilotSearchCommand extends AbstractCommand {
   
   private static final int DEFAULT_RESULTS = 20;

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      // Get the command results
      CommandResult result = ctx.getResult();

      // Check if we're doing a GET
      if (ctx.getParameter("firstName") == null) {
         ctx.setAttribute("noResults", Boolean.TRUE, REQUEST);
         ctx.setAttribute("maxResults", new Integer(DEFAULT_RESULTS), REQUEST);
         result.setURL("/jsp/roster/pilotSearch.jsp");
         result.setSuccess(true);
         return;
      }

      // Check if we're doing an exact match
      boolean exactMatch = Boolean.valueOf(ctx.getParameter("exactMatch")).booleanValue();

      // Build the parameters
      String fName = buildParameter(ctx.getParameter("firstName"), exactMatch);
      String lName = buildParameter(ctx.getParameter("lastName"), exactMatch);
      String eMail = buildParameter(ctx.getParameter("eMail"), exactMatch);

      // Check the pilot code parameter
      int pilotCode = getPilotCode(ctx.getParameter("pilotCode"));

      Collection<Pilot> results = null;
      try {
         Connection con = ctx.getConnection();

         // Get the DAO and set the result size
         GetPilot dao = new GetPilot(con);
         try {
            int maxResults = Integer.parseInt(ctx.getParameter("maxResults"));
            if ((maxResults < 1) || (maxResults > 99))
               throw new IllegalArgumentException();
            
            dao.setQueryMax(maxResults);
            ctx.setAttribute("maxResults", new Integer(maxResults), REQUEST);
         } catch (Exception e) {
            dao.setQueryMax(DEFAULT_RESULTS);
            ctx.setAttribute("maxResults", new Integer(DEFAULT_RESULTS), REQUEST);
         }
         
         // Get the search results
         if (pilotCode > 0) {
            results = new ArrayList<Pilot>();
            Pilot p = dao.getPilotByCode(pilotCode, SystemData.get("airline.db"));
            if (p != null)
               results.add(p);
         } else {
            results = dao.search(fName, lName, eMail);
         }
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Calculate access to each search result
      Map<Integer, PilotAccessControl> accessMap = new HashMap<Integer, PilotAccessControl>();
      for (Iterator<Pilot> i = results.iterator(); i.hasNext();) {
         Pilot p = i.next();

         // Calculate the access level
         PilotAccessControl access = new PilotAccessControl(ctx, p);
         access.validate();

         // Save the access level in the map, indexed by pilot ID
         accessMap.put(new Integer(p.getID()), access);
      }

      // Save the results and access level in the request
      ctx.setAttribute("results", results, REQUEST);
      ctx.setAttribute("accessMap", accessMap, REQUEST);

      // Forward to the JSP
      result.setURL("/jsp/roster/pilotSearch.jsp");
      result.setSuccess(true);
   }

   /**
    * Helper method to take a parameter and add LIKE wildcards.
    */
   private String buildParameter(String pValue, boolean exMatch) {
      if (StringUtils.isEmpty(pValue)) return null;
      return exMatch ? pValue : "%" + pValue + "%";
   }

   /**
    * Helper method to convert a pilot code to a numeric value.
    */
   private int getPilotCode(CharSequence pcValue) {

      StringBuilder buf = new StringBuilder();
      for (int x = 0; x < pcValue.length(); x++) {
         char c = pcValue.charAt(x);
         if (Character.isDigit(c)) buf.append(c);
      }

      // Convert the code
      try {
         return Integer.parseInt(buf.toString());
      } catch (NumberFormatException nfe) {
         return 0;
      }
   }
}