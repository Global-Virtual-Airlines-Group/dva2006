// Copyright 2005, 2006, 2008, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.security.command.*;
import org.deltava.util.*;

/**
 * A Web Site Command to display Examination Question Profiles.
 * @author Luke
 * @version 3.6
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
	@Override
   public void execute(CommandContext ctx) throws CommandException {

      // Get the view start/end/count
      ViewContext<QuestionProfile> vc = initView(ctx, QuestionProfile.class);

      // Get the exam name
      String examName = (String) ctx.getCmdParameter(Command.ID, "ALL");
      try {
         Connection con = ctx.getConnection();
         
         // Get the examination profiles and see which ones we can view
         boolean hasAcademy = false; boolean hasProgram = false;
         GetExamProfiles epdao = new GetExamProfiles(con);
         Collection<ExamProfile> eProfiles = epdao.getExamProfiles();
         for (Iterator<ExamProfile> i = eProfiles.iterator(); i.hasNext(); ) {
        	 ExamProfile ep = i.next();
        	 ExamProfileAccessControl eac = new ExamProfileAccessControl(ctx, ep);
        	 eac.validate();
        	 if (eac.getCanRead()) {
        		 hasAcademy |= ep.getAcademy();
        		 hasProgram |= !ep.getAcademy();
        	 } else
        		 i.remove();
         }
         
         // Determine what stats to display
         boolean isAcademyOnly = hasAcademy && !hasProgram;
         isAcademyOnly |= Boolean.valueOf(ctx.getParameter("isAcademy")).booleanValue();
         
         // Get all exam names and save
         List<Object> examNames = new ArrayList<Object>(ADD_OPTS);
         examNames.addAll(eProfiles);
         ctx.setAttribute("examNames", examNames, REQUEST);
         ctx.setAttribute("academyOnly", Boolean.valueOf(isAcademyOnly), REQUEST);
         
         // Get the question list and save
         GetExamQuestions eqdao = new GetExamQuestions(con);
         eqdao.setQueryStart(vc.getStart());
         eqdao.setQueryMax(vc.getCount());
         int ofs = StringUtils.arrayIndexOf(ADD_CODES, examName);
         switch (ofs) {
         case 1:
        	 vc.setResults(eqdao.getMostPopular(isAcademyOnly));
        	 break;
        	 
         case 2:
         case 3:
        	 int minExams = StringUtils.parse(ctx.getParameter("minExams"), 20);
        	 vc.setResults(eqdao.getResults((ofs == 2), isAcademyOnly, minExams));
        	 ctx.setAttribute("minExams", Integer.valueOf(minExams), REQUEST);
        	 break;
        	 
         default:
        	ExamProfile ep = epdao.getExamProfile(examName);
        	if (ep != null) {
        		ExamProfileAccessControl eac = new ExamProfileAccessControl(ctx, ep);
        		eac.validate();
        		if (!eac.getCanRead())
        			throw securityException("Cannot view " + examName + " examination info");
        	}

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