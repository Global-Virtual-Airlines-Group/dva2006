// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.assign;

import java.sql.Connection;
import java.util.Iterator;

import org.deltava.beans.FlightReport;
import org.deltava.beans.assign.AssignmentInfo;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to save a Flight Assignment.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AssignmentSaveCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error (typically database) occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      // Check for the flight assignment
      AssignmentInfo info = (AssignmentInfo) ctx.getSession().getAttribute("buildAssign");
      if (info == null)
          throw new CommandException("Flight Assignment data not in session");

      try {
          Connection con = ctx.getConnection();

          // Create the Flight Assignment
          SetAssignment awdao = new SetAssignment(con);
          awdao.write(info, SystemData.get("airline.db"));

          // If the assignment has a pilot linked with it, write the draft PIREPs
          if ((info.getStatus() == AssignmentInfo.RESERVED) && (info.getPilotID() != 0)) {
             ctx.setAttribute("pirepsWritten", Boolean.valueOf(true), REQUEST);
              SetFlightReport pwdao = new SetFlightReport(con);
              for (Iterator i = info.getFlights().iterator(); i.hasNext();) {
                  FlightReport fr = (FlightReport) i.next();
                  pwdao.write(fr);
              }
          }

          // Set attributes and clean up session
          ctx.setAttribute("isCreate", Boolean.valueOf(true), REQUEST);
          ctx.setAttribute("assign", info, REQUEST);
          ctx.getSession().removeAttribute("buildAssign");
      } catch (DAOException de) {
          throw new CommandException(de);
      } finally {
          ctx.release();
      }

      // Redirect to the update page
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/assign/assignUpdate.jsp");
      result.setType(CommandResult.REQREDIRECT);
      result.setSuccess(true);
   }
}