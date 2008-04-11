// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ExamAccessControl;

/**
 * A Web Site Command to view/take/score Pilot Examinations.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class ExamCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      // Get the results
      CommandResult result = ctx.getResult();
      try {
         Connection con = ctx.getConnection();
         
         // Get the examination
         GetExam dao = new GetExam(con);
         Examination ex = dao.getExam(ctx.getID());
         if (ex == null)
            throw notFoundException("Invalid Examination - " + ctx.getID());
         
         // Get the Pilot taking the exam
         GetUserData uddao = new GetUserData(con);
         UserData ud = uddao.get(ex.getPilotID());
         
         // Load the examination profile
         GetExamProfiles epdao = new GetExamProfiles(con);
         ExamProfile ep = epdao.getExamProfile(ex.getName());
         if (ep == null)
            throw notFoundException("Cannot load Examination Profile - " + ex.getName());
         
         // Check our access level
         ExamAccessControl access = new ExamAccessControl(ctx, ex, ud, ep);
         access.validate();
         if (!access.getCanRead())
            throw securityException("Cannot view Examination " + ctx.getID());
         
         // Get the Pilot and the scorer
         Collection<Integer> IDs = new HashSet<Integer>();
         IDs.add(new Integer(ex.getPilotID()));
         if (ex.getScorerID() != 0)
        	 IDs.add(new Integer(ex.getScorerID()));
         
         // Load the Pilots
         GetPilot pdao = new GetPilot(con);
         UserDataMap udm = uddao.get(IDs);
         Map<Integer, Pilot> pilots = pdao.get(udm);
         ctx.setAttribute("pilot", pilots.get(new Integer(ex.getPilotID())), REQUEST);
       	 ctx.setAttribute("scorer", pilots.get(new Integer(ex.getScorerID())), REQUEST);
         
         // Display answers only if we have the necessary role
         int activeExamID = dao.getActiveExam(ctx.getUser().getID());
         if (ex.getPilotID() == ctx.getUser().getID())
         	ctx.setAttribute("showAnswers", Boolean.valueOf(access.getCanViewAnswers() && (activeExamID == 0)), REQUEST);
         else
         	ctx.setAttribute("showAnswers", Boolean.valueOf(access.getCanViewAnswers()), REQUEST);
         
         // Determine what we will do with the examination
         String opName = (String) ctx.getCmdParameter(Command.OPERATION, null);
         if (access.getCanSubmit()) {
            // Calculate time remaining
            ctx.setAttribute("timeRemaining", new Long((ex.getExpiryDate().getTime() - System.currentTimeMillis()) / 1000), REQUEST);
            result.setURL("/jsp/testing/examTake.jsp");
         } else if ((ex.getStatus() != Test.SCORED) && access.getCanScore())
            result.setURL("/jsp/testing/examScore.jsp");
         else if (("edit".equals(opName)) && (access.getCanEdit()))
            result.setURL("/jsp/testing/examScore.jsp");
         else
            result.setURL("/jsp/testing/examView.jsp");
         
         // Save the exam and access in the request
         ctx.setAttribute("exam", ex, REQUEST);
         ctx.setAttribute("access", access, REQUEST);
         ctx.setAttribute("hasQImages", Boolean.valueOf(ex.hasImage()), REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Forward to the JSP
      result.setSuccess(true);
   }
}