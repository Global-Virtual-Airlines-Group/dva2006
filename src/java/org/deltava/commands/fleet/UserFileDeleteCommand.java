// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.fleet;

import java.sql.Connection;

import org.deltava.beans.fleet.FileEntry;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.FileEntryAccessControl;

/**
 * A Web Site Command to delete User File Library entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class UserFileDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
   public void execute(CommandContext ctx) throws CommandException {
      
      // Get the file to delete
      String fName = (String) ctx.getCmdParameter(ID, null);
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the library entry
         GetLibrary dao = new GetLibrary(con);
         FileEntry entry = dao.getFile(fName);
         if (entry == null)
            throw notFoundException("Invalid file name - " + fName);
         
         // Check our access
         FileEntryAccessControl access = new FileEntryAccessControl(ctx, entry);
         access.validate();
         if (!access.getCanDelete())
            throw securityException("Cannot delete File Library entry");
         
         // Delete the entry
         SetLibrary wdao = new SetLibrary(con);
         wdao.delete(entry);
         
         // Save the entry
         ctx.setAttribute("libraryEntry", entry, REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Set status attributes
      ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);
      ctx.setAttribute("library", "User File", REQUEST);
      ctx.setAttribute("librarycmd", "filelibrary", REQUEST);

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setType(ResultType.REQREDIRECT);
      result.setURL("/jsp/fleet/libraryUpdate.jsp");
      result.setSuccess(true);
   }
}