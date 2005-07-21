// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.testing.*;

import org.deltava.commands.*;

import org.deltava.dao.GetExam;
import org.deltava.dao.SetExam;
import org.deltava.dao.DAOException;

import org.deltava.security.command.ExamAccessControl;

/**
 * A Web Site Command to submit Pilot Examinations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ExamSubmitCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {
      
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the examination
         GetExam rdao = new GetExam(con);
         Examination ex = rdao.getExam(ctx.getID());
         if (ex == null)
            throw new CommandException("Invalid Examination - " + ctx.getID());
         
         // Check our access level
         ExamAccessControl access = new ExamAccessControl(ctx, ex);
         access.validate();
         if (!access.getCanSubmit())
            throw new CommandSecurityException("Cannot submit Examination");
         
         // Save answers from the request
         List questions = ex.getQuestions();
         for (int x = 0; x < questions.size(); x++) {
            Question q = (Question) questions.get(x);
            q.setAnswer(ctx.getParameter("answer" + String.valueOf(q.getNumber() - 1))); // JSP fields are 1-offset
         }
         
         // Set the status of the examination, and submitted date
         Calendar cld = Calendar.getInstance();
         ex.setStatus(Test.SUBMITTED);
         ex.setSubmittedOn(cld.getTime());
         
         // Write the examination to the database
         SetExam wdao = new SetExam(con);
         wdao.update(ex);
         
         // Save the exam to the request
         ctx.setAttribute("isSubmit", Boolean.TRUE, REQUEST);
         ctx.setAttribute("exam", ex, REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setType(CommandResult.REQREDIRECT);
      result.setURL("/jsp/testing/examUpdate.jsp");
      result.setSuccess(true);
   }
}