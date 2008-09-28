// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

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
             throw notFoundException("Invalid Flight Report - " + ctx.getID());
          
          // Check our access level
          PIREPAccessControl access = new PIREPAccessControl(ctx, fr);
          access.validate();
          if (!access.getCanDelete())
             throw securityException("Cannot delete Flight Report");
          
          // Start a JDBC transaction
          ctx.startTX();
          
          // Get the DAO and delete the PIREP from the database
          SetFlightReport wdao = new SetFlightReport(con);
          wdao.delete(ctx.getID());
          
          // If this is an ACARS PIREP, delete the data
          if (fr instanceof ACARSFlightReport) {
             SetACARSLog awdao = new SetACARSLog(con);
             awdao.deleteInfo(fr.getDatabaseID(FlightReport.DBID_ACARS));
          }
          
          // Commit the transaction
          ctx.commitTX();
      } catch (DAOException de) {
         ctx.rollbackTX();
          throw new CommandException(de);
      } finally {
          ctx.release();
      }
      
      // Update the status for the JSP
      ctx.setAttribute("isDeleted", Boolean.TRUE, REQUEST);

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setType(ResultType.REQREDIRECT);
      result.setURL("/jsp/pilot/pirepUpdate.jsp");
      result.setSuccess(true);
   }
}