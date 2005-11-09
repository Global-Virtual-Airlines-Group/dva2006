// Copyright 2005 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.StatusUpdate;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PilotAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to merge two pilot profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class DuplicatePilotMergeCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {
      
      // Get the pilot IDs to merge
      Collection ids = new HashSet();
      String[] mergeIDs = ctx.getRequest().getParameterValues("sourceID");
      for (int x = 0; x < mergeIDs.length; x++)
         ids.add(new Integer(StringUtils.parseHex(mergeIDs[x])));

      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the pilots
         GetPilot dao = new GetPilot(con);
         Collection src = dao.getByID(ids, "PILOTS").values();
         Pilot usr = dao.get(ctx.getID());
         if (usr == null)
            throw new CommandException("Invalid User - " + ctx.getID());
         
         // Validate our access
         for (Iterator i = src.iterator(); i.hasNext(); ) {
            Pilot p = (Pilot) i.next();
            PilotAccessControl access = new PilotAccessControl(ctx, p);
            access.validate();
            if (!access.getCanChangeStatus())
               i.remove();
         }
         
         // Check that we can merge any pilots
         if (src.isEmpty())
            throw securityException("Cannot merge Pilots");
         
         // Start a JDBC transaction
         ctx.startTX();
         
         // Iterate through the Pilots
         Collection sUpdates = new ArrayList();
         SetPilotMerge mgdao = new SetPilotMerge(con);
         for (Iterator i = src.iterator(); i.hasNext(); ) {
            Pilot p = (Pilot) i.next();
            
            // Create a status update
            StatusUpdate su = new StatusUpdate(p.getID(), StatusUpdate.STATUS_CHANGE);
            su.setAuthorID(ctx.getUser().getID());
            su.setDescription("Merged into " + usr.getName() + " (" + usr.getPilotCode() + ")");
            sUpdates.add(su);
            
            // Migrate the data
            mgdao.merge(p, usr);
         }

         // Write status updates
         SetStatusUpdate sudao = new SetStatusUpdate(con);
         sudao.write(sUpdates);
         
         // Commit the transaction
         ctx.commitTX();
         
         // Save the pilots
         ctx.setAttribute("pilot", usr, REQUEST);
         ctx.setAttribute("oldPilots", src, REQUEST);
      } catch (DAOException de) {
         ctx.rollbackTX();
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setType(CommandResult.REQREDIRECT);
      result.setURL("/jsp/roster/dupeMerge.jsp");
      result.setSuccess(true);
   }
}