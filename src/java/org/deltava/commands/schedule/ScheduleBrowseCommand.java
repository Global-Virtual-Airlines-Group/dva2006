// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.schedule.*;

import org.deltava.comparators.AirportComparator;

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
      
      // Save the search criteria
      ctx.setAttribute("airportD", aD, REQUEST);
      ctx.setAttribute("airportA", criteria.getAirportA(), REQUEST);
      
      // Create the airport comparator and sorted result sets
      AirportComparator<Airport> ac = new AirportComparator<Airport>(AirportComparator.NAME);
      Set<Airport> airportsD = new TreeSet<Airport>(ac);
      Set<Airport> airportsA = new TreeSet<Airport>(ac);
      
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and source/destination airports
         GetSchedule dao = new GetSchedule(con);
         airportsD.addAll(dao.getOriginAirports(null));
         airportsA.addAll(dao.getConnectingAirports(criteria.getAirportD(), true));
         
         // Save airports
         ctx.setAttribute("airports", airportsD, REQUEST);
         ctx.setAttribute("dstAP", airportsA, REQUEST);
         
         // Do the search
         dao.setQueryStart(vc.getStart());
         dao.setQueryMax(vc.getCount());
         vc.setResults(dao.search(criteria, "AIRPORT_D, AIRPORT_A"));
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