// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.assign;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.FlightReport;
import org.deltava.beans.assign.AssignmentInfo;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to save a Flight Assignment.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class AssignmentSaveCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error (typically database) occurs
    */
   public void execute(CommandContext ctx) throws CommandException {
	   
	   // Get command results
	   CommandResult result = ctx.getResult();

      // Check for the flight assignment
      AssignmentInfo info = (AssignmentInfo) ctx.getSession().getAttribute("buildAssign");
      if (info == null) {
    	  ctx.setMessage("Flight Assignment data not found - Session Timeout?");
    	  result.setURL("/jsp/schedule/findAflight.jsp");
          result.setType(CommandResult.REQREDIRECT);
          result.setSuccess(true);
          return;
      }

      try {
          Connection con = ctx.getConnection();
          
          // Start the transaction
          ctx.startTX();

          // Create the Flight Assignment
          SetAssignment awdao = new SetAssignment(con);
          awdao.write(info, SystemData.get("airline.db"));

          // If the assignment has a pilot linked with it, write the draft PIREPs
          if ((info.getStatus() == AssignmentInfo.RESERVED) && (info.getPilotID() != 0)) {
             Date now = new Date();
             info.setAssignDate(now);
             awdao.assign(info, info.getPilotID(), SystemData.get("airline.db"));
             
             // Write the PIREPs to the database
             ctx.setAttribute("pirepsWritten", Boolean.TRUE, REQUEST);
              SetFlightReport pwdao = new SetFlightReport(con);
              for (Iterator<FlightReport> i = info.getFlights().iterator(); i.hasNext();) {
                  FlightReport fr = i.next();
                  fr.setDate(now);
                  fr.setRank(ctx.getUser().getRank());
                  pwdao.write(fr);
              }
          }
          
          // Commit the transaction
          ctx.commitTX();
      } catch (DAOException de) {
         ctx.rollbackTX();
          throw new CommandException(de);
      } finally {
          ctx.release();
      }
      
      // Set attributes and clean up session
      ctx.setAttribute("pilot", ctx.getUser(), REQUEST);
      ctx.setAttribute("isCreate", Boolean.TRUE, REQUEST);
      ctx.setAttribute("assign", info, REQUEST);
      ctx.getSession().removeAttribute("buildAssign");
      ctx.getSession().removeAttribute("fafCriteria");

      // Redirect to the update page
      result.setURL("/jsp/assign/assignUpdate.jsp");
      result.setType(CommandResult.REQREDIRECT);
      result.setSuccess(true);
   }
}