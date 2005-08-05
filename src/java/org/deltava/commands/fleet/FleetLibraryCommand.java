package org.deltava.commands.fleet;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;
import org.deltava.beans.fleet.*;
import org.deltava.commands.*;

import org.deltava.dao.GetLibrary;
import org.deltava.dao.DAOException;

import org.deltava.security.command.FleetEntryAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display the Fleet Library.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FleetLibraryCommand extends AbstractCommand {

   private static final Logger log = Logger.getLogger(FleetLibraryCommand.class);

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      // Check if we're viewing the library as an administrator
      boolean isAdmin = "admin".equals(ctx.getCmdParameter(Command.OPERATION, null));

      // Check our access
      FleetEntryAccessControl access = new FleetEntryAccessControl(ctx, null);
      access.validate();
      if (isAdmin && !access.getCanCreate())
         throw new CommandSecurityException("Cannot update Fleet Library");

      List results = null;
      try {
         Connection con = ctx.getConnection();

         // Get the DAO and the Fleet Library
         GetLibrary dao = new GetLibrary(con);
         results = dao.getFleet(SystemData.get("airline.db"));

         // Get the fleet libraries from the other airlines if we're not in admin mode
         if (!isAdmin) {
            Map dbs = (Map) SystemData.getObject("airlineDatabases");
            for (Iterator i = dbs.keySet().iterator(); i.hasNext();) {
               String dbName = (String) i.next();
               results.addAll(dao.getFleet(dbName));
            }
         }
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Validate our access to the results
      for (Iterator i = results.iterator(); i.hasNext();) {
         FleetEntry e = (FleetEntry) i.next();
         access.setEntry(e);
         access.validate();

         // If we cannot view this entry, remove it from the list
         if (!access.getCanView())
            i.remove();

         // If the entry is not present on the file system, remove it
         if (e.getSize() == 0) {
            log.warn("Resource " + e.getFullName() + " not found in file system!");
            if (!isAdmin)
               i.remove();
         }
      }

      // Save the results in the request
      ctx.setAttribute("fleet", results, REQUEST);

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL(isAdmin ? "/jsp/fleet/installerLibrary.jsp" : "/jsp/fleet/fleetLibrary.jsp");
      result.setSuccess(true);
   }
}