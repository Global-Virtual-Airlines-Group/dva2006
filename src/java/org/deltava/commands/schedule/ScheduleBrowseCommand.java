// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
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
      
      // Get the departure airport
      Airport aD = SystemData.getAirport(ctx.getParameter("airportD"));
      if (aD == null)
    	  aD = SystemData.getAirport(ctx.getUser().getHomeAirport());
      
      // Build the search criteria
      ScheduleSearchCriteria criteria = new ScheduleSearchCriteria(null, 0, 0);
      criteria.setAirportD(aD);
      criteria.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
      criteria.setDBName(SystemData.get("airline.db"));
      criteria.setIncludeAcademy(ctx.isUserInRole("Instructor") || ctx.isUserInRole("Schedule") || ctx.isUserInRole("HR"));
      
      // Save the search criteria
      ctx.setAttribute("airportD", aD, REQUEST);
      ctx.setAttribute("airportA", criteria.getAirportA(), REQUEST);
      
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and source/destination airports
         GetScheduleAirport dao = new GetScheduleAirport(con);
         ctx.setAttribute("airports", dao.getOriginAirports(null), REQUEST);
         ctx.setAttribute("dstAP", dao.getConnectingAirports(criteria.getAirportD(), true, null), REQUEST);
         
         // Do the search
         GetSchedule sdao = new GetSchedule(con);
         sdao.setQueryStart(vc.getStart());
         sdao.setQueryMax(vc.getCount());
         vc.setResults(sdao.search(criteria, "AIRPORT_D, AIRPORT_A"));
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