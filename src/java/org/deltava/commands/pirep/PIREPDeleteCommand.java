// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.pirep;

import java.sql.Connection;

import org.deltava.beans.FlightReport;
import org.deltava.commands.*;

import org.deltava.dao.GetFlightReports;
import org.deltava.dao.SetFlightReport;
import org.deltava.dao.DAOException;

import org.deltava.security.command.PIREPAccessControl;

/**
 * A Web Site Command to delete Flight Reports.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PIREPDeleteCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      try {
          Connection con = ctx.getConnection();
          
          // Get the DAO and the Flight Report
          GetFlightReports dao = new GetFlightReports(con);
          FlightReport fr = dao.get(ctx.getID());
          if (fr == null)
             throw new CommandException("Invalid Flight Report - " + ctx.getID());
          
          // Check our access level
          PIREPAccessControl access = new PIREPAccessControl(ctx, fr);
          access.validate();
          if (!access.getCanDelete())
             throw new CommandSecurityException("Cannot delete Flight Report");
          
          // Get the DAO and delete the PIREP from the database
          SetFlightReport wdao = new SetFlightReport(con);
          wdao.delete(ctx.getID());

          // Update the status for the JSP
          ctx.setAttribute("isDeleted", Boolean.valueOf(true), REQUEST);
      } catch (DAOException de) {
          throw new CommandException(de);
      } finally {
          ctx.release();
      }

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setType(CommandResult.REQREDIRECT);
      result.setURL("/jsp/pilot/pirepUpdate.jsp");
      result.setSuccess(true);
   }
}