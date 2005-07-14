// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.testing;

import java.sql.Connection;

import org.deltava.beans.testing.CheckRide;
import org.deltava.commands.*;

import org.deltava.dao.GetExam;
import org.deltava.dao.GetPilot;
import org.deltava.dao.DAOException;

import org.deltava.security.command.ExamAccessControl;

import org.deltava.util.ComboUtils;

/**
 * A Web Site Command to view Check Ride records.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CheckRideCommand extends AbstractCommand {
   
   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {
      
      // Get the command result
      CommandResult result = ctx.getResult();
      
      try {
         Connection con = ctx.getConnection();
         
         // Load the check ride data 
         GetExam dao = new GetExam(con);
         CheckRide cr = dao.getCheckRide(ctx.getID());
         
         // Check our access
         ExamAccessControl access = new ExamAccessControl(ctx, cr);
         access.validate();
         if (!access.getCanRead())
            throw new CommandSecurityException("Cannot view Check Ride");
         
         // Load the pilot data
         GetPilot pdao = new GetPilot(con);
         ctx.setAttribute("pilot", pdao.get(cr.getPilotID()), REQUEST);
         if (cr.getScorerID() != 0)
            ctx.setAttribute("scorer", pdao.get(cr.getScorerID()), REQUEST);
         
         // Save the checkride and the access controller
         ctx.setAttribute("checkRide", cr, REQUEST);
         ctx.setAttribute("access", access, REQUEST);
         ctx.setAttribute("passFail", ComboUtils.fromArray(new String[] {"PASS", "FAIL"}, new String[] {"0", "1"}), REQUEST);
         
         // Check if we can score/edit or not
         boolean doEdit = access.getCanScore() || access.getCanSubmit();
         result.setURL(doEdit ? "/jsp/testing/cRideScore.jsp" : "/jsp/testing/cRideRead.jsp");
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Forward to the JSP
      result.setSuccess(true);
   }
}