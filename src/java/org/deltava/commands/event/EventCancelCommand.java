// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.event;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.FlightReport;
import org.deltava.beans.UserDataMap;
import org.deltava.beans.assign.AssignmentInfo;
import org.deltava.beans.event.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.EventAccessControl;

/**
 * A Web Site Command to cancel an Online Event.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EventCancelCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      try {
         Connection con = ctx.getConnection();

         // Get the DAO and the event
         GetEvent dao = new GetEvent(con);
         Event e = dao.get(ctx.getID());
         if (e == null)
            throw notFoundException("Unknown Online Event - " + ctx.getID());
         
         // Get the signed up users for this event
         GetUserData uddao = new GetUserData(con);
         UserDataMap udmap = uddao.getByEvent(e.getID());

         // Check our access level
         EventAccessControl access = new EventAccessControl(ctx, e);
         access.validate();
         if (!access.getCanCancel())
            throw securityException("Cannot cancel Online Event " + e.getName());

         // Initialize counters
         int flightsDeleted = 0;
         int flightsUpdated = 0;

         // Start the transaction
         ctx.startTX();

         // Get all the PIREPs for this Event, and either remove the event ID or delete them
         GetFlightReports frdao = new GetFlightReports(con);
         SetFlightReport fwdao = new SetFlightReport(con);
         for (Iterator<String> i = udmap.getTableNames().iterator(); i.hasNext(); ) {
        	 String tableName = i.next();
        	 Collection<FlightReport> pireps = frdao.getByEvent(e.getID(), tableName.substring(0, tableName.indexOf('.')));
        	 for (Iterator<FlightReport> pi = pireps.iterator(); pi.hasNext();) {
        		 FlightReport fr = pi.next();
                 if (fr.getStatus() == FlightReport.DRAFT) {
                     fwdao.delete(fr.getID());
                     flightsDeleted++;
                  } else {
                     fr.setDatabaseID(FlightReport.DBID_ASSIGN, 0);
                     fr.setDatabaseID(FlightReport.DBID_EVENT, 0);
                     fwdao.write(fr);
                     flightsUpdated++;
                  }
        	 }
         }
         
         // Save flight totals
         ctx.setAttribute("flightsDeleted", new Integer(flightsDeleted), REQUEST);
         ctx.setAttribute("flightsUpdated", new Integer(flightsUpdated), REQUEST);

         // Get the assignments for the event and delete them
         GetAssignment ardao = new GetAssignment(con);
         SetAssignment awdao = new SetAssignment(con);
         for (Iterator<String> ti = udmap.getTableNames().iterator(); ti.hasNext(); ) {
        	 String tableName = ti.next();
        	 Collection<AssignmentInfo> assignments = ardao.getByEvent(e.getID(), tableName);
        	 for (Iterator<AssignmentInfo> i = assignments.iterator(); i.hasNext();) {
        		 AssignmentInfo ai = i.next();
                 awdao.delete(ai);
              }	 
         }
         
         // Cancel the event
         SetEvent wdao = new SetEvent(con);
         e.setStatus(Event.CANCELED);
         wdao.write(e);

         // Commit the transaction
         ctx.commitTX();
         
         // Seve the event in the request
         ctx.setAttribute("event", e, REQUEST);
      } catch (DAOException de) {
         ctx.rollbackTX();
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Update status for the JSP
      ctx.setAttribute("isCancel", Boolean.TRUE, REQUEST);

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setType(CommandResult.REQREDIRECT);
      result.setURL("/jsp/event/eventUpdate.jsp");
      result.setSuccess(true);
   }
}