// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.acars;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.system.UserDataMap;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Site Command to view the ACARS Flight Info log.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FlightLogCommand extends ACARSLogViewCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      // Get the view context and the search type
      ViewContext vc = initView(ctx);
      int searchType = getSearchType(ctx);

      // Get the command result
      CommandResult result = ctx.getResult();
      
      // If we're not displaying anything, redirect to the result page
      if (ctx.getParameter("searchType") == null) {
         result.setURL("/jsp/acars/flightLog.jsp");
         result.setSuccess(true);
         return;
      }
      
      // If we're doing a search by Pilot ID, validate the ID
      if ((searchType == SEARCH_USR) && (!validatePilotCode(ctx.getParameter("pilotCode")))) {
         ctx.setMessage("Invalid Pilot Code");
         result.setURL("/jsp/acars/msgLog.jsp");
         return;
      }

      try {
         Connection con = ctx.getConnection();

         // If we're doing a search by Pilot ID, then translate to user ID
         int pilotID = 0;
         if (searchType == SEARCH_USR) {
            UserID id = new UserID(ctx.getParameter("pilotCode"));
            
            GetPilot pdao = new GetPilot(con);
            Pilot usr = pdao.getPilotByCode(id.getUserID(), id.getAirlineCode());
            pilotID = (usr == null) ? 0 : usr.getID();
         } else if (searchType == SEARCH_ID) {
            pilotID = Integer.parseInt(ctx.getParameter("pilotCode"));
         }

         // Get the DAO and set start/count parameters
         GetACARSLog dao = new GetACARSLog(con);
         dao.setQueryStart(vc.getStart());
         dao.setQueryMax(vc.getCount());

         // Depending on the search type, call the DAO query
         if (searchType == SEARCH_DATE) {
         	Date sd = parseDateTime(ctx, "start", "MM/dd/yyyy", "HH:mm");
         	Date ed = parseDateTime(ctx, "end", "MM/dd/yyyy", "HH:mm");
            vc.setResults(dao.getFlights(sd, ed));
            
            // Save start/end dates
            ctx.setAttribute("startDate", sd, REQUEST);
            ctx.setAttribute("endDate", ed, REQUEST);
         } else if (searchType == SEARCH_LATEST) {
            vc.setResults(dao.getMessages());
         } else {
            vc.setResults(dao.getFlights(pilotID));
         }
         
         // Load the Pilot data
         GetUserData usrdao = new GetUserData(con);
         UserDataMap udm = usrdao.get(getPilotIDs(vc.getResults()));
         ctx.setAttribute("userData", udm, REQUEST);
         
			// Get the authors for each log entry
			Map pilots = new HashMap();
			GetPilot pdao = new GetPilot(con);
			for (Iterator i = udm.getTableNames().iterator(); i.hasNext(); ) {
				String dbTableName = (String) i.next();
				pilots.putAll(pdao.getByID(udm.getByTable(dbTableName), dbTableName));
			}

			// Save the pilots in the request
         ctx.setAttribute("pilots", pilots, REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Set search complete attribute
      ctx.setAttribute("doSearch", Boolean.TRUE, REQUEST);
      
      // Forward to the JSP
      result.setURL("/jsp/acars/flightLog.jsp");
      result.setSuccess(true);
   }
}