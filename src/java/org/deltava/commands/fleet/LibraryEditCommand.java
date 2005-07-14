// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.fleet;

import java.sql.Connection;

import org.deltava.beans.fleet.*;
import org.deltava.commands.*;

import org.deltava.dao.GetLibrary;
import org.deltava.dao.DAOException;

import org.deltava.security.command.FleetEntryAccessControl;

import org.deltava.util.ComboUtils;

/**
 * A Web Site Command to support editing Fleet/Document Library entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class LibraryEditCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      // Get the file name, or if we're creating a new file
      String fName = (String) ctx.getCmdParameter(Command.ID, "NEW");
      ctx.setAttribute("securityOptions", ComboUtils.fromArray(FleetEntry.SECURITY_LEVELS), REQUEST);

      // Figure out what type of entry we are attempting to create
      boolean isManual = "manual".equals(ctx.getCmdParameter(Command.OPERATION, "fleet"));

      // Get the command results
      CommandResult result = ctx.getResult();

      // If we're creating a new entry, check our access
      if ("NEW".equals(fName)) {
         FleetEntryAccessControl access = new FleetEntryAccessControl(ctx, null);
         access.validate();
         if (!access.getCanEdit()) throw new CommandSecurityException("Cannot create Library Entry");

         // Forward to the JSP
         result.setURL(isManual ? "/jsp/fleet/manualEdit.jsp" : "/jsp/fleet/installerEdit.jsp");
         result.setSuccess(true);
         return;
      }

      FleetEntry entry = null;
      try {
         Connection con = ctx.getConnection();

         // Get the DAO and the library entry
         GetLibrary dao = new GetLibrary(con);
         if (isManual) {
            entry = dao.getManual(fName);
         } else {
            entry = dao.getInstaller(fName);
         }
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Check our access level
      FleetEntryAccessControl access = new FleetEntryAccessControl(ctx, entry);
      access.validate();
      if (!access.getCanEdit()) throw new CommandSecurityException("Cannot edit Library Entry for " + fName);

      // Save the entry in the request
      ctx.setAttribute("entry", entry, REQUEST);
      ctx.setAttribute("access", access, REQUEST);

      // Forward to the JSP
      result.setURL(isManual ? "/jsp/fleet/manualEdit.jsp" : "/jsp/fleet/installerEdit.jsp");
      result.setSuccess(true);
   }
}