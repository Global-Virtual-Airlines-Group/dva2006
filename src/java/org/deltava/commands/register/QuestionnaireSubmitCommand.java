// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.register;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.testing.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

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

         // Set the status of the examination, and submitted date
         ex.setStatus(Test.SUBMITTED);
         ex.setSubmittedOn(new Date());
         
         // Save answers from the request
         for (int x = 1; x <= ex.getSize(); x++) {
            Question q = ex.getQuestion(x);
            q.setAnswer(ctx.getParameter("answer" + String.valueOf(x)));
         }
         
         // Get EMail validation results
         GetAddressValidation avdao = new GetAddressValidation(con);
         ctx.setAttribute("addrValid", avdao.get(ex.getPilotID()), REQUEST);

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
      result.setURL("/jsp/register/qComplete.jsp");
      result.setSuccess(true);
   }
}