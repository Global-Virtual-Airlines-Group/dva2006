// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.testing.ExamProfile;

import org.deltava.commands.*;

import org.deltava.dao.GetExamProfiles;
import org.deltava.dao.DAOException;

import org.deltava.security.command.ExamProfileAccessControl;

/**
 * A Web Site Command to display Examination Profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ExamProfilesCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      Collection<ExamProfile> results = null;
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the exam profile list
         GetExamProfiles dao = new GetExamProfiles(con);
         results = dao.getExamProfiles();
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Check our access
      for (Iterator<ExamProfile> i = results.iterator(); i.hasNext(); ) {
    	  ExamProfile ep = i.next();
    	  ExamProfileAccessControl access = new ExamProfileAccessControl(ctx, ep);
    	  access.validate();
    	  if (!access.getCanRead())
    		  i.remove();
      }
      
      // Save in the request
      ctx.setAttribute("examProfiles", results, REQUEST);
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/testing/examProfiles.jsp");
      result.setSuccess(true);
   }
}