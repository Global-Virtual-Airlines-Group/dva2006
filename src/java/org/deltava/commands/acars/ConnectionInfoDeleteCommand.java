// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.acars;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.acars.ConnectionEntry;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to delete ACARS Connection log entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ConnectionInfoDeleteCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {
      
      // Get the connection IDs
      List conIDs = Arrays.asList(ctx.getRequest().getParameterValues("conID"));
      
      Set deletedIDs = new HashSet();
      Set skippedIDs = new HashSet();
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the ACARS log entry
         GetACARSLog dao = new GetACARSLog(con);
         
         // Start the transaction
         ctx.startTX();
         
         // Delete the connection entries
         for (Iterator i = conIDs.iterator(); i.hasNext(); ) {
            long id = Long.parseLong((String) i.next());
            
            ConnectionEntry c = dao.getConnection(id);
            if (c == null)
               throw new CommandException("Invalid ACARS Connection ID - " + id);
            
            // Check for a Flight Information Report
            if (c.getFlightInfoCount() > 0) {
               ctx.setAttribute("info", dao.getInfo(id), REQUEST);
               skippedIDs.add(StringUtils.formatHex(c.getID()));
            } else {
               SetACARSLog wdao = new SetACARSLog(con);
               wdao.deleteConnection(id);
               
               // Set the status attribute
               deletedIDs.add(StringUtils.formatHex(c.getID()));
            }
         }
         
         // Commit the transaction
         ctx.commitTX();
      } catch (DAOException de) {
         ctx.rollbackTX();
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Set status attributes
      ctx.setAttribute("conDelete", Boolean.TRUE, REQUEST);
      ctx.setAttribute("deletedIDs", deletedIDs, REQUEST);
      ctx.setAttribute("skippedIDs", skippedIDs, REQUEST);
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setType(CommandResult.REQREDIRECT);
      result.setURL("/jsp/acars/logEntryDelete.jsp");
      result.setSuccess(true);
   }
}