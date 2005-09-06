// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.schedule;

import java.sql.Connection;

import org.deltava.beans.schedule.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to browse the Flight Schedule.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ScheduleBrowseCommand extends AbstractViewCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      // Get the view context
      ViewContext vc = initView(ctx);
      
      // Build the search criteria
      ScheduleSearchCriteria criteria = new ScheduleSearchCriteria(null, 0, 0);
      criteria.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
      criteria.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
      
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO
         GetSchedule dao = new GetSchedule(con);
         
         // Get connecting airports
         ctx.setAttribute("dstAP", dao.getConnectingAirports(criteria.getAirportD(), true), REQUEST);
         
         // Do the search
         dao.setQueryStart(vc.getStart());
         dao.setQueryMax(vc.getCount());
         vc.setResults(dao.search(criteria, false));
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/schedule/browse.jsp");
      result.setSuccess(true);
   }
}