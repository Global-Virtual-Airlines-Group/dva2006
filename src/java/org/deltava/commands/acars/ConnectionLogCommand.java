// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.acars;

import java.sql.Connection;

import org.deltava.beans.Pilot;

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
      if (ctx.getCmdParameter(Command.ID, null) == null) {
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
            pilotID = ctx.getID();
         }

         // Get the DAO and set start/count parameters
         GetACARSLog dao = new GetACARSLog(con);
         dao.setQueryStart(vc.getStart());
         dao.setQueryMax(vc.getCount());

         // Depending on the search type, call the DAO query
         if (searchType == SEARCH_DATE) {
            vc.setResults(dao.getConnections(getDate(ctx, "startDate"), getDate(ctx, "endDate")));
         } else {
            vc.setResults(dao.getConnections(pilotID));
         }
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Forward to the JSP
      result.setURL("/jsp/acars/connectionLog.jsp");
      result.setSuccess(true);
   }
}