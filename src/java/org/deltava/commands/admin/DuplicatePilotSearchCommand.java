// Copyright 2005 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.sql.Connection;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to search for duplicate Pilots. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class DuplicatePilotSearchCommand extends AbstractCommand {
   
   private static final int DEFAULT_RESULTS = 5;

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {
      
      // Get the command results
      CommandResult result = ctx.getResult();
      if (ctx.getParameter("firstName") == null) {
         ctx.setAttribute("noResults", Boolean.TRUE, REQUEST);
         ctx.setAttribute("maxResults", new Integer(DEFAULT_RESULTS), REQUEST);
         result.setURL("/jsp/roster/dupeSearch.jsp");
         result.setSuccess(true);
         return;
      }
      
      // Check if we're doing an exact match
      boolean exactMatch = Boolean.valueOf(ctx.getParameter("exactMatch")).booleanValue();

      // Build the parameters
      String fName1 = buildParameter(ctx.getParameter("firstName"), exactMatch);
      String lName1 = buildParameter(ctx.getParameter("lastName"), exactMatch);
      String fName2 = buildParameter(ctx.getParameter("firstName2"), exactMatch);
      String lName2 = buildParameter(ctx.getParameter("lastName2"), exactMatch);

      Collection results = null;
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
         
         // Do the search
         results = dao.search(fName1, lName1, null);
         results.addAll(dao.search(fName2, lName2, null));
         
         // Save the results in the request
         ctx.setAttribute("results", results, REQUEST);

      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Forward to the JSP
      result.setURL("/jsp/roster/dupeSearch.jsp");
      result.setSuccess(true);
   }

   /**
    * Helper method to take a parameter and add LIKE wildcards.
    */
   private String buildParameter(String pValue, boolean exMatch) {
      if (StringUtils.isEmpty(pValue)) return null;
      return exMatch ? pValue : "%" + pValue + "%";
   }
}