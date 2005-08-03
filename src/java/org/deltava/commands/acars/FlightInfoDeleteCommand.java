// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.acars;

import java.sql.Connection;

import org.deltava.beans.ACARSFlightReport;
import org.deltava.beans.acars.FlightInfo;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to delete ACARS Flight Info entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FlightInfoDeleteCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {
      try {
         Connection con = ctx.getConnection();
         
         // Get the Flight Info record
         GetACARSLog acdao = new GetACARSLog(con);
         FlightInfo info = acdao.getInfo(ctx.getID());
         if (info == null)
            throw new CommandException("Invalid ACARS Flight ID - " + ctx.getID());
         
         // Make sure the flight doesn't have a PIREP linked to it
         GetFlightReports frdao = new GetFlightReports(con);
         ACARSFlightReport afr = frdao.getACARS(info.getID());
         if (afr != null) {
            ctx.setAttribute("pirep", afr, REQUEST);
         } else {
            SetACARSLog wdao = new SetACARSLog(con);
            wdao.deleteInfo(info.getID());
            
            // Set the status attribute
            ctx.setAttribute("infoDelete", Boolean.TRUE, REQUEST);
         }
         
         // Save the Flight info record in the request
         ctx.setAttribute("info", info, REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setType(CommandResult.REQREDIRECT);
      result.setURL("/jsp/acars/logEntryDelete.jsp");
      result.setSuccess(true);
   }
}