// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.sql.Connection;

import org.deltava.beans.schedule.Chart;
import org.deltava.commands.*;

import org.deltava.dao.GetChart;
import org.deltava.dao.SetChart;
import org.deltava.dao.DAOException;

import org.deltava.security.command.ChartAccessControl;

/**
 * A Web Site Command to delete Approach Charts.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ChartDeleteCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {
      
      // Check our access
      ChartAccessControl access = new ChartAccessControl(ctx);
      access.validate();
      if (!access.getCanDelete())
         throw securityException("Cannot delete Approach Chart");

      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the Chart
         GetChart dao = new GetChart(con);
         Chart c = dao.get(ctx.getID());
         if (c == null)
            throw notFoundException("Invalid Approach Chart - " + ctx.getID());
         
         // Save the chart in the request
         ctx.setAttribute("chart", c, REQUEST);
         
         // Delete the chart
         SetChart wdao = new SetChart(con);
         wdao.delete(c.getID());
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Set status variable for the JSP
      ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setType(ResultType.REQREDIRECT);
      result.setURL("/jsp/schedule/chartUpdate.jsp");
      result.setSuccess(true);
   }
}