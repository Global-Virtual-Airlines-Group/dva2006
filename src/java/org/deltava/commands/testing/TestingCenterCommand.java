// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display the Testing Center.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TestingCenterCommand extends AbstractTestHistoryCommand {
   
   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      List allExams = null;
      
      // Get the user Profile
      Pilot usr = (Pilot) ctx.getUser();
      ctx.setAttribute("pilot", usr, REQUEST);
      
      try {
         Connection con = ctx.getConnection();
         
         // Initialize the Testing History
         initTestHistory(usr, con);

         // Get all Examination Profiles
         GetExamProfiles epdao = new GetExamProfiles(con);
         allExams = epdao.getExamProfiles();
         
         // Remove all examinations that we have passed or require a higher stage than us
         for (Iterator i = allExams.iterator(); i.hasNext(); ) {
            ExamProfile ep = (ExamProfile) i.next();
            if (!_testHistory.canWrite(ep))
               i.remove();
         }

         // Save the remaining exam profiles in the request
         ctx.setAttribute("exams", _testHistory.getExams(), REQUEST);
         ctx.setAttribute("availableExams", allExams, REQUEST);

         // Check if we have an examination open
         GetExam exdao = new GetExam(con);
         int activeExamID = exdao.getActiveExam(usr.getID());
         ctx.setAttribute("examActive", new Integer(activeExamID), REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/testing/testCenter.jsp");
      result.setSuccess(true);
   }
}