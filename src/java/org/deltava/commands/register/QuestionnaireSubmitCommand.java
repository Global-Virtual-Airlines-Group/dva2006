// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.register;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.testing.*;
import org.deltava.commands.*;

import org.deltava.dao.GetQuestionnaire;
import org.deltava.dao.SetQuestionnaire;
import org.deltava.dao.DAOException;

import org.deltava.security.command.QuestionnaireAccessControl;

/**
 * A Web Site Command for submitting Applicant Questionnaires.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class QuestionnaireSubmitCommand extends AbstractCommand {

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
         if (!access.getCanSubmit())
            throw new CommandSecurityException("Cannot submit Questionnaire");
         
         // Save answers from the request
         List questions = ex.getQuestions();
         for (int x = 0; x < questions.size(); x++) {
            Question q = (Question) questions.get(x);
            q.setAnswer(ctx.getParameter("answer" + String.valueOf(x)));
         }

         // Set the status of the examination, and submitted date
         Calendar cld = Calendar.getInstance();
         ex.setStatus(Test.SUBMITTED);
         ex.setSubmittedOn(cld.getTime());

         // Write the examination to the database
         SetQuestionnaire wdao = new SetQuestionnaire(con);
         wdao.write(ex);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setType(CommandResult.REDIRECT);
      result.setURL("appqsubmit", null, null);
      result.setSuccess(true);
   }
}