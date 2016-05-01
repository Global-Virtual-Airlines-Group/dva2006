// Copyright 2005, 2006, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.util.*;

import org.deltava.beans.testing.ExamProfile;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ExamProfileAccessControl;

/**
 * A Web Site Command to display Examination Profiles.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class ExamProfilesCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   @Override
   public void execute(CommandContext ctx) throws CommandException {

      Collection<ExamProfile> results = null;
      try {
         GetExamProfiles dao = new GetExamProfiles(ctx.getConnection());
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
      
      // Check our access to create profiles
      ExamProfileAccessControl access = new ExamProfileAccessControl(ctx, null);
      access.validate();
      
      // Save in the request
      ctx.setAttribute("examProfiles", results, REQUEST);
      ctx.setAttribute("access", access, REQUEST);
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/testing/examProfiles.jsp");
      result.setSuccess(true);
   }
}