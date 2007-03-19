// Copyright 2005, 2006, 2007 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.ExamAccessControl;

/**
 * A Web Site Command to score Pilot Examinations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ExamScoreCommand extends AbstractCommand {
   
   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {
   	
   	// Create the messaging context
   	MessageContext mctxt = new MessageContext();
   	mctxt.addData("user", ctx.getUser());

   	Pilot usr = null;
      try {
         Connection con = ctx.getConnection();
         
         // Get the examination
         GetExam rdao = new GetExam(con);
         Examination ex = rdao.getExam(ctx.getID());
         if (ex == null)
            throw notFoundException("Invalid Examination - " + ctx.getID());
         
         // Check our access level
         ExamAccessControl access = new ExamAccessControl(ctx, ex);
         access.validate();
         if (!access.getCanScore())
            throw securityException("Cannot score Examination");
         
         // Load the examination profile
         GetExamProfiles epdao = new GetExamProfiles(con);
         ExamProfile ep = epdao.getExamProfile(ex.getName());
         if (ep == null)
            throw notFoundException("Cannot load Examination Profile - " + ex.getName());
         
         // Calculate the score
         int score = 0;
         for (int x = 1; x <= ex.getSize(); x++) {
            Question q = ex.getQuestion(x);
            boolean isCorrect = Boolean.valueOf(ctx.getParameter("Score" + String.valueOf(x))).booleanValue();
            q.setCorrect(isCorrect);
            if (isCorrect)
               score++;
         }
         
         // Get the Pilot profile
         GetPilot pdao = new GetPilot(con);
         usr = pdao.get(ex.getPilotID());
         ctx.setAttribute("pilot", usr, REQUEST);
         mctxt.addData("pilot", usr);
         
         // Get the Message template
         GetMessageTemplate mtdao = new GetMessageTemplate(con);
         mctxt.setTemplate(mtdao.get("EXAMSCORE"));
         mctxt.addData("exam", ex);
         
         // Check if we've passed the examination
         ex.setPassFail(score >= ep.getPassScore());
         mctxt.addData("result", ex.getPassFail() ? "PASS" : "UNSATISFACTORY");
         
         // Check if we've rescored the examination
         ctx.setAttribute("reScore", Boolean.valueOf(ex.getStatus() == Test.SCORED), REQUEST);
         
         // Update examination
         Calendar cld = Calendar.getInstance();
         ex.setScoredOn(cld.getTime());
         ex.setStatus(Test.SCORED);
         ex.setScore(score);
         ex.setScorerID(ctx.getUser().getID());
         ex.setComments(ctx.getParameter("comments"));
         
         // Update the examination in the database
         SetExam wdao = new SetExam(con);
         wdao.update(ex);
         
         // Save the exam in the request
         ctx.setAttribute("isScore", Boolean.TRUE, REQUEST);
         ctx.setAttribute("exam", ex, REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Send the notification message
      Mailer mailer = new Mailer(ctx.getUser());
      mailer.setContext(mctxt);
      mailer.send(usr);
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setType(CommandResult.REQREDIRECT);
      result.setURL("/jsp/testing/examUpdate.jsp");
      result.setSuccess(true);
   }
}