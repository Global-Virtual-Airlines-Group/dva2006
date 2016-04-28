// Copyright 2005, 2006, 2009, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.assign.AssignmentInfo;
import org.deltava.beans.flight.FlightReport;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.AssignmentAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display a Flight Assignment.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class AssignmentCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
   @Override
   public void execute(CommandContext ctx) throws CommandException {

      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the Assignment
         GetAssignment dao = new GetAssignment(con);
         AssignmentInfo assign = dao.get(ctx.getID());
         if (assign == null)
            throw notFoundException("Invalid Flight Assignment - " + ctx.getID());
         
         // Calculate our access
         AssignmentAccessControl access = new AssignmentAccessControl(ctx, assign);
         access.validate();
         ctx.setAttribute("access", access, REQUEST);
         
         // Load the Flight Reports for this Assignment
         GetFlightReports frdao = new GetFlightReports(con);
         List<FlightReport> pireps = frdao.getByAssignment(ctx.getID(), SystemData.get("airline.db"));
         pireps.forEach(fr -> assign.addFlight(fr));
         ctx.setAttribute("assign", assign, REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/assign/assignView.jsp");
      result.setSuccess(true);
   }
}