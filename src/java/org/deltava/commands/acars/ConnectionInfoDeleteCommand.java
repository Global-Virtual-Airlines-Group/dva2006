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
      Collection<String> conIDs = ctx.getParameters("conID");
      
      Set<String> deletedIDs = new HashSet<String>();
      Set<String> skippedIDs = new HashSet<String>();
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAOs
         GetACARSLog dao = new GetACARSLog(con);
         SetACARSLog wdao = new SetACARSLog(con);
         
         // Start the transaction
         ctx.startTX();
         
         // Delete the connection entries
         for (Iterator i = conIDs.iterator(); i.hasNext(); ) {
            long id = Long.parseLong((String) i.next());
            
            // Get the Connection entry - check for a Flight Information Report
            ConnectionEntry c = dao.getConnection(id);
            if (c == null) {
               skippedIDs.add(StringUtils.formatHex(id));
            } else if (c.getFlightInfoCount() > 0) {
               skippedIDs.add(StringUtils.formatHex(id));
            } else {
               wdao.deleteConnection(id);
               deletedIDs.add(StringUtils.formatHex(id));
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
      result.setType(ResultType.REQREDIRECT);
      result.setURL("/jsp/acars/logEntryDelete.jsp");
      result.setSuccess(true);
   }
}