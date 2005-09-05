// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.register;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.testing.Examination;
import org.deltava.beans.testing.Question;
import org.deltava.beans.testing.Test;

import org.deltava.commands.*;

import org.deltava.dao.*;

import org.deltava.security.command.QuestionnaireAccessControl;

/**
 * A Web Site Command for scoring Applicant Questionnaires.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class QuestionnaireScoreCommand extends AbstractCommand {
   
   private static final Logger log = Logger.getLogger(QuestionnaireScoreCommand.class);

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the questionnaire
         GetQuestionnaire rdao = new GetQuestionnaire(con);
         Examination ex = rdao.get(ctx.getID());
         if (ex == null)
            throw new CommandException("Invalid Questionnaire - " + ctx.getID());
         
         // Check our access level
         QuestionnaireAccessControl access = new QuestionnaireAccessControl(ctx, ex);
         access.validate();
         if (!access.getCanScore())
            throw securityException("Cannot score Questionnaire");
         
         // Calculate the score
         int score = 0;
         for (int x = 1; x <= ex.getSize(); x++) {
            Question q = ex.getQuestion(x);
            boolean isCorrect = "1".equals(ctx.getParameter("Score" + String.valueOf(x)));
            if (isCorrect) {
               log.debug("Question #" + String.valueOf(x) + " is correct");
               q.setCorrect(true);
               score++;
            }
         }
         
         // Update examination
         Calendar cld = Calendar.getInstance();
         ex.setScoredOn(cld.getTime());
         ex.setStatus(Test.SCORED);
         ex.setScore(score);
         ex.setScorerID(ctx.getUser().getID());
         ex.setPassFail(true);

         // Get the Applicant profile
         GetApplicant adao = new GetApplicant(con);
         ctx.setAttribute("applicant", adao.get(ex.getPilotID()), REQUEST);

         // Update the questionnaire in the database
         SetQuestionnaire wdao = new SetQuestionnaire(con);
         wdao.write(ex);

         // Save the questionnaire in the request
         ctx.setAttribute("isScore", Boolean.valueOf(true), REQUEST);
         ctx.setAttribute("exam", ex, REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setType(CommandResult.REQREDIRECT);
      result.setURL("/jsp/register/qUpdate.jsp");
      result.setSuccess(true);
   }
}