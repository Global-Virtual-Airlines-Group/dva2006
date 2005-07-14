// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.testing;

import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.testing.Test;
import org.deltava.beans.testing.Examination;

import org.deltava.commands.*;

import org.deltava.dao.GetExam;
import org.deltava.dao.GetPilot;
import org.deltava.dao.DAOException;

import org.deltava.security.command.ExamAccessControl;

/**
 * A Web Site Command to view/take/score Pilot Examinations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ExamCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      // Get the results
      CommandResult result = ctx.getResult();
      try {
         Connection con = ctx.getConnection();
         
         // Get the examination
         GetExam dao = new GetExam(con);
         Examination ex = dao.getExam(ctx.getID());
         if (ex == null)
            throw new CommandException("Invalid Examination - " + ctx.getID());
         
         // Check our access level
         ExamAccessControl access = new ExamAccessControl(ctx, ex);
         access.validate();
         if (!access.getCanRead())
            throw new CommandSecurityException("Cannot view Examination " + ctx.getID());
         
         // Get the Pilot
         GetPilot pdao = new GetPilot(con);
         Pilot p = pdao.get(ex.getPilotID());
         ctx.setAttribute("pilot", p, REQUEST);
         
         // See if we have an examination open
         int activeExamID = dao.getActiveExam(ctx.getUser().getID());
         if (ex.getPilotID() == ctx.getUser().getID()) {
         	ctx.setAttribute("showAnswers", Boolean.valueOf(ex.getPassFail() && (activeExamID == 0)), REQUEST);
         } else {
         	ctx.setAttribute("showAnswers", Boolean.valueOf(activeExamID == 0), REQUEST);
         }
         
         // Determine what we will do with the examination
         String opName = (String) ctx.getCmdParameter(Command.OPERATION, null);
         if (access.getCanSubmit()) {
            result.setURL("/jsp/testing/examTake.jsp");
         } else if ((ex.getStatus() == Test.SUBMITTED) && access.getCanScore()) {
            result.setURL("/jsp/testing/examScore.jsp");
         } else if (("edit".equals(opName)) && (access.getCanEdit())) {
            result.setURL("/jsp/testing/examScore.jsp");
         } else {
            result.setURL("/jsp/testing/examView.jsp");
         }
         
         // Save the exam and access in the request
         ctx.setAttribute("exam", ex, REQUEST);
         ctx.setAttribute("access", access, REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Forward to the JSP
      result.setSuccess(true);
   }
}