// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.pirep;

import java.sql.Connection;

import org.deltava.beans.ACARSFlightReport;

import org.deltava.commands.*;

import org.deltava.dao.GetFlightReports;
import org.deltava.dao.DAOException;

/**
 * A Web Site Command to view Flight Reports from a Check Ride.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CheckRidePIREPCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error (typically database) occurs
	 */
   public void execute(CommandContext ctx) throws CommandException {

      int pirepID;
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the ACARS Flight Report
         GetFlightReports dao = new GetFlightReports(con);
         ACARSFlightReport afr = dao.getACARS(ctx.getID());
         if (afr == null)
            throw new CommandException("Invalid ACARS Flight ID - " + ctx.getID());
         
         // Save the flight ID - we'll pass this to the PIREP command
         pirepID = afr.getID();
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Forward to the PIREP command - its read method does a lot of work we do not want to duplicate
      CommandResult result = ctx.getResult();
      result.setType(CommandResult.REDIRECT);
      result.setURL("pirep", null, pirepID);
      result.setSuccess(true);
   }
}