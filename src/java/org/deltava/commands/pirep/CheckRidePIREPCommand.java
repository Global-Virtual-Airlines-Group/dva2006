// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.testing.CheckRide;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

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
      boolean crossDB = false;
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the Check Ride
         GetExam exdao = new GetExam(con);
         CheckRide cr = exdao.getCheckRide(ctx.getID());
         if (cr == null)
        	 throw notFoundException("Invalid Check Ride ID - " + ctx.getID());
         
         // Get the Pilot data
         GetUserData uddao = new GetUserData(con);
         UserData ud = uddao.get(cr.getPilotID());
         if (ud == null)
        	 throw notFoundException("Invalid Pilot ID - " + cr.getPilotID());
         
         // Check if we're loading from another DB
         crossDB = !SystemData.get("airline.db").equals(ud.getDB());
         
         // Get the DAO and the ACARS Flight Report
         GetFlightReports dao = new GetFlightReports(con);
         ACARSFlightReport afr = dao.getACARS(ud.getDB(), cr.getFlightID());
         if (afr == null)
            throw notFoundException("Invalid ACARS Flight ID - " + cr.getFlightID());
         
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
      result.setURL(crossDB ? "extpirep" : "pirep", null, pirepID);
      result.setSuccess(true);
   }
}