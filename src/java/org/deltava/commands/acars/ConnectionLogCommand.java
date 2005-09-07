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
 * A Web Site Command to display ACARS Connection Log entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ConnectionLogCommand extends ACARSLogViewCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error (typically database) occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      // Get the view context and the search type
      ViewContext vc = initView(ctx);
      int searchType = getSearchType(ctx);

      // Get the command result
      CommandResult result = ctx.getResult();

      // If we're not displaying anything, redirect to the result page
      if (ctx.getParameter("searchType") == null) {
         result.setURL("/jsp/acars/connectionLog.jsp");
         result.setSuccess(true);
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
            vc.setResults(dao.getConnections(sd, ed));
         } else {
            vc.setResults(dao.getConnections(pilotID));
         }
         
         // Load the Pilot data
         GetUserData usrdao = new GetUserData(con);
         UserDataMap udm = usrdao.get(getPilotIDs(vc.getResults()));
         ctx.setAttribute("userData", udm, REQUEST);
         
			// Get the users for each connection
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
      result.setURL("/jsp/acars/connectionLog.jsp");
      result.setSuccess(true);
   }
}