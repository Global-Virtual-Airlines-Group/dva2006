// Copyright 2005, 2006, 2007, 2012, 2016, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.register;

import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.testing.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.QuestionnaireAccessControl;

/**
 * A Web Site Command for scoring Applicant Questionnaires.
 * @author Luke
 * @version 10.2
 * @since 1.0
 */

public class QuestionnaireScoreCommand extends AbstractCommand {
   
   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
	@Override
   public void execute(CommandContext ctx) throws CommandException {
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the questionnaire
         GetQuestionnaire rdao = new GetQuestionnaire(con);
         Examination ex = rdao.get(ctx.getID());
         if (ex == null)
            throw notFoundException("Invalid Questionnaire - " + ctx.getID());
         
         // Check our access level
         QuestionnaireAccessControl access = new QuestionnaireAccessControl(ctx, ex);
         access.validate();
         if (!access.getCanScore())
            throw securityException("Cannot score Questionnaire");
         
         // Calculate the score
         int score = 0;
         for (int x = 1; x <= ex.getSize(); x++) {
            Question q = ex.getQuestion(x);
            boolean isCorrect = Boolean.parseBoolean(ctx.getParameter("Score" + String.valueOf(x)));
            q.setCorrect(isCorrect);
            if (isCorrect)
               score++;
         }
         
         // Update examination
         ex.setScoredOn(Instant.now());
         ex.setStatus(TestStatus.SCORED);
         ex.setScore(score);
         ex.setScorerID(ctx.getUser().getID());
         ex.setPassFail(true);

         // Get the Applicant profile
         GetApplicant adao = new GetApplicant(con);
         ctx.setAttribute("applicant", adao.get(ex.getAuthorID()), REQUEST);

         // Update the questionnaire in the database
         SetQuestionnaire wdao = new SetQuestionnaire(con);
         wdao.write(ex);

         // Save the questionnaire in the request
         ctx.setAttribute("isScore", Boolean.TRUE, REQUEST);
         ctx.setAttribute("questionnaire", ex, REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setType(ResultType.REQREDIRECT);
      result.setURL("/jsp/register/qUpdate.jsp");
      result.setSuccess(true);
   }
}