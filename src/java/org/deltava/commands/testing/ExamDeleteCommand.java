// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.testing;

import java.sql.Connection;

import org.deltava.beans.UserData;
import org.deltava.beans.testing.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ExamAccessControl;

/**
 * A Web Site Command to delete Pilot Examinations and Check Rides.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ExamDeleteCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      // Check if we're deleting an exam or a checkride
      boolean isCheckRide = "checkride".equals(ctx.getCmdParameter(Command.OPERATION, null));
      
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the Examination or Check Ride
         GetExam dao = new GetExam(con);
         Test t =  isCheckRide ? (Test) dao.getCheckRide(ctx.getID()) : (Test) dao.getExam(ctx.getID());
         if (t == null)
            throw notFoundException("Invalid " + (isCheckRide ? "Check Ride - " : "Examination - ") + ctx.getID());
         
         // Get the user data
         GetUserData uddao = new GetUserData(con);
         UserData ud = uddao.get(t.getPilotID());
         
         // Check our access
         ExamAccessControl access = new ExamAccessControl(ctx, t, ud);
         access.validate();
         if (!access.getCanDelete())
            throw securityException("Cannot delete Examination/Check Ride");
         
         // Get the write DAO and delete the test
         SetExam wdao = new SetExam(con);
         wdao.delete(t);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Set status for the JSP
      ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);
      ctx.setAttribute("isCheckRide", Boolean.valueOf(isCheckRide), REQUEST);
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setType(CommandResult.REQREDIRECT);
      result.setURL("/jsp/testing/examUpdate.jsp");
      result.setSuccess(true);
   }
}