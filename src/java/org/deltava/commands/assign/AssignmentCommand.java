// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.FlightReport;
import org.deltava.beans.assign.AssignmentInfo;

import org.deltava.commands.*;

import org.deltava.dao.GetAssignment;
import org.deltava.dao.GetFlightReports;
import org.deltava.dao.DAOException;

import org.deltava.security.command.AssignmentAccessControl;

/**
 * A Web Site Command to display a Flight Assignment.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AssignmentCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
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
         
         // Load the Flight Reports for this Assignment
         GetFlightReports frdao = new GetFlightReports(con);
         List pireps = frdao.getByAssignment(ctx.getID());
         for (Iterator i = pireps.iterator(); i.hasNext(); ) {
            FlightReport fr = (FlightReport) i.next();
            assign.addFlight(fr);
         }
         
         // Save the assignment and access controller
         ctx.setAttribute("assign", assign, REQUEST);
         ctx.setAttribute("access", access, REQUEST);
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