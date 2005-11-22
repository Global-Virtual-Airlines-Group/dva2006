// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.system.TransferRequest;

import org.deltava.commands.*;

import org.deltava.dao.GetPilot;
import org.deltava.dao.GetTransferRequest;
import org.deltava.dao.DAOException;

/**
 * A Web Site Command to list pending equipment program Transfer Requests.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TransferListCommand extends AbstractViewCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {
      
      // Initialize the view context
      ViewContext vc = initView(ctx);
      
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the transfer requests
         GetTransferRequest txdao = new GetTransferRequest(con);
         txdao.setQueryStart(vc.getStart());
         txdao.setQueryMax(vc.getCount());
         vc.setResults(txdao.getAll());
         
         // Build a set of Pilot IDs
         Set<Integer> pilotIDs = new HashSet<Integer>();
         for (Iterator i = vc.getResults().iterator(); i.hasNext(); ) {
            TransferRequest txreq = (TransferRequest) i.next();
            pilotIDs.add(new Integer(txreq.getID()));
         }
         
         // Save the Pilot beans in the request
         GetPilot pdao = new GetPilot(con);
         ctx.setAttribute("pilots", pdao.getByID(pilotIDs, "PILOTS"), REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/admin/txRequestList.jsp");
      result.setSuccess(true);
   }
}