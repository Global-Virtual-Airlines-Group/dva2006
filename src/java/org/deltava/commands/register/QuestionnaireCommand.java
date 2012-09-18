// Copyright 2005, 2006, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.register;

import java.sql.Connection;

import org.deltava.beans.Applicant;
import org.deltava.beans.testing.Examination;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.QuestionnaireAccessControl;

/**
 * A Web Site Command to handle the initial applicant questionnaire.
 * @author Luke
 * @version 5.0
 * @since 1.0
 */

public class QuestionnaireCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
	@Override
   public void execute(CommandContext ctx) throws CommandException {
      
      CommandResult result = ctx.getResult();
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the Questionnaire
         GetQuestionnaire exdao = new GetQuestionnaire(con);
         Examination ex = exdao.get(ctx.getID());
         if (ex == null)
            throw notFoundException("Invalid Questionnaire - " + ctx.getID());
         
         // Get our access and fail gracefully if we can no longer read it
         QuestionnaireAccessControl access = new QuestionnaireAccessControl(ctx, ex);
         access.validate();
         if (!access.getCanRead() && !ctx.isAuthenticated()) {
            ctx.release();
            result.setURL("/jsp/register/qNoAccess.jsp");
            result.setSuccess(true);
            return;
         } else if (!access.getCanRead())
            throw securityException("Cannot view Applicant Questionnaire");
         
         // Get the Applicant profile
         GetApplicant adao = new GetApplicant(con);
         Applicant a = adao.get(ex.getAuthorID());
         if (a == null)
         	throw notFoundException("Invalid Applicant ID - " + ex.getAuthorID());
         
         // Save the questionnaire and the access controller
         ctx.setAttribute("exam", ex, REQUEST);
         ctx.setAttribute("applicant", a, REQUEST);
         ctx.setAttribute("access", access, REQUEST);
         ctx.setAttribute("hasQImages", Boolean.valueOf(ex.hasImage()), REQUEST);
         
         // Determine the JSP to forward to
         if (access.getCanSubmit())
         	result.setURL("/jsp/register/qSubmit.jsp");
         else if (access.getCanScore())
         	result.setURL("/jsp/register/qScore.jsp");
         else
         	result.setURL("/jsp/register/qView.jsp");
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      result.setSuccess(true);
   }
}