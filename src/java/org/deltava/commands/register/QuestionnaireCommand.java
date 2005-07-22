// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.register;

import java.sql.Connection;

import org.deltava.beans.Applicant;
import org.deltava.beans.testing.Examination;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.QuestionnaireAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to handle the initial applicant questionaire.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class QuestionnaireCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {
      
      // Get the command result
      CommandResult result = ctx.getResult();
      
      // Since the ID may not be hex-encoded we need to grab it a different way
      Object idP = ctx.getCmdParameter(Command.ID, "0");
      int id = (idP instanceof Integer) ? ((Integer) idP).intValue() : StringUtils.parseHex((String) idP);

      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the Questionnaire
         GetQuestionnaire exdao = new GetQuestionnaire(con);
         Examination ex = exdao.get(id);
         if (ex == null)
            throw new CommandException("Invalid Questionnaire - " + id);
         
         // Get our access and fail gracefully if we can no longer read it
         QuestionnaireAccessControl access = new QuestionnaireAccessControl(ctx, ex);
         access.validate();
         if (!access.getCanRead() && !ctx.isAuthenticated()) {
            ctx.release();
            result.setURL("/jsp/register/qNoAccess.jsp");
            result.setSuccess(true);
            return;
         } else if (!access.getCanRead()) {
            throw new CommandSecurityException("Cannot view Applicant Questionnaire");
         }
         
         // Get the Applicant profile
         GetApplicant adao = new GetApplicant(con);
         Applicant a = adao.get(ex.getPilotID());
         if (a == null)
         	throw new CommandException("Invalid Applicant ID - " + ex.getPilotID());
         
         // Save the questionnaire and the access controller
         ctx.setAttribute("exam", ex, REQUEST);
         ctx.setAttribute("applicant", a, REQUEST);
         ctx.setAttribute("access", access, REQUEST);
         
         // Determine the JSP to forward to
         if (access.getCanSubmit()) {
         	result.setURL("/jsp/register/qSubmit.jsp");
         } else if (access.getCanScore()) {
         	result.setURL("/jsp/register/qScore.jsp");
         } else {
         	result.setURL("/jsp/register/qView.jsp");
         }
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Forward to the JSP
      result.setSuccess(true);
   }
}