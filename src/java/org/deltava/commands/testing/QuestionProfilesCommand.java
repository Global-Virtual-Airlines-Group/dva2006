// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.GetExamProfiles;
import org.deltava.dao.DAOException;

import org.deltava.security.command.QuestionProfileAccessControl;

import org.deltava.util.ComboUtils;

/**
 * A Web Site Command to display Examination Question Profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class QuestionProfilesCommand extends AbstractViewCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      // Check our access
      QuestionProfileAccessControl access = new QuestionProfileAccessControl(ctx, null);
      access.validate();
      ctx.setAttribute("access", access, REQUEST);
      
      // Get the view start/end/count
      ViewContext vc = initView(ctx);

      // Get the exam name
      String examName = (String) ctx.getCmdParameter(Command.ID, "ALL");
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO
         GetExamProfiles dao = new GetExamProfiles(con);
         
         // Get all exam names and save
         List<Object> examNames = new ArrayList<Object>(dao.getExamProfiles());
         examNames.add(0, ComboUtils.fromString("All Examinations", "ALL"));
         ctx.setAttribute("examNames", examNames, REQUEST);
         
         // Get the question list and save
         dao.setQueryStart(vc.getStart());
         dao.setQueryMax(vc.getCount());
         vc.setResults(dao.getQuestionPool(examName, false));
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