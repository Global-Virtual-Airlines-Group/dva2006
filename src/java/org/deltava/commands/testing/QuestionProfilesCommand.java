// Copyright 2005, 2006, 2008, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.testing.ExamProfile;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.QuestionProfileAccessControl;

import org.deltava.util.ComboUtils;
import org.deltava.util.StringUtils;

/**
 * A Web Site Command to display Examination Question Profiles.
 * @author Luke
 * @version 3.5
 * @since 1.0
 */

public class QuestionProfilesCommand extends AbstractViewCommand {
	
	private static final String[] ADD_NAMES = new String[] {"All Examinations", "Most Popular", "Highest Scoring", "Lowest Scoring"}; 
	private static final String[] ADD_CODES = new String[] {"ALL", "MP", "HS", "LS"};
	
	private static final Collection<ComboAlias> ADD_OPTS = ComboUtils.fromArray(ADD_NAMES, ADD_CODES);

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      // Get the view start/end/count
      ViewContext vc = initView(ctx);

      // Get the exam name
      String examName = (String) ctx.getCmdParameter(Command.ID, "ALL");
      try {
         Connection con = ctx.getConnection();
         
         // Get the examination profile
         GetExamProfiles epdao = new GetExamProfiles(con);
         
         
         // Get all exam names and save
         List<Object> examNames = new ArrayList<Object>(ADD_OPTS);
         examNames.addAll(epdao.getExamProfiles());
         ctx.setAttribute("examNames", examNames, REQUEST);
         
         // Get the question list and save
         GetExamQuestions eqdao = new GetExamQuestions(con);
         eqdao.setQueryStart(vc.getStart());
         eqdao.setQueryMax(vc.getCount());
         int ofs = StringUtils.arrayIndexOf(ADD_CODES, examName);
         switch (ofs) {
         case 1:
        	 vc.setResults(eqdao.getMostPopular());
        	 break;
        	 
         case 2:
        	 vc.setResults(eqdao.getResults(true, StringUtils.parse(ctx.getParameter("minExams"), 10)));
        	 break;
        	 
         case 3:
        	 vc.setResults(eqdao.getResults(false, StringUtils.parse(ctx.getParameter("minExams"), 10)));
        	 break;
        	 
         default:
        	ExamProfile ep = epdao.getExamProfile(examName);
        	vc.setResults(eqdao.getQuestions(ep));
         }
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Check our access
      QuestionProfileAccessControl access = new QuestionProfileAccessControl(ctx, null);
      access.validate();
      ctx.setAttribute("access", access, REQUEST);
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/testing/questionProfiles.jsp");
      result.setSuccess(true);
   }
}