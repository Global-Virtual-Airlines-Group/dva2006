// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.testing;

import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.GetExamProfiles;
import org.deltava.dao.DAOException;

import org.deltava.security.command.QuestionProfileAccessControl;

/**
 * A Web Site Command to display Examination Question Profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class QuestionProfilesCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      // Check our access
      QuestionProfileAccessControl access = new QuestionProfileAccessControl(ctx);
      access.validate();
      if (!access.getCanRead())
         throw new CommandSecurityException("Not Authorized");

      // Get the exam name
      String examName = (String) ctx.getCmdParameter(Command.ID, "ALL");
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the exam profile list
         GetExamProfiles dao = new GetExamProfiles(con);
         ctx.setAttribute("questionProfiles", dao.getQuestionPool(examName), REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/testing/questionProfiles.jsp");
      result.setSuccess(true);
   }
}