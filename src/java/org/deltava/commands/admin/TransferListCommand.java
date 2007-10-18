// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.UserDataMap;
import org.deltava.beans.system.TransferRequest;

import org.deltava.commands.*;
import org.deltava.dao.*;

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
      String eqType = ctx.getParameter("eqType");
      boolean allEQ = (eqType == null) || ("-".equals(eqType));
      
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the transfer requests
         GetTransferRequest txdao = new GetTransferRequest(con);
         txdao.setQueryStart(vc.getStart());
         txdao.setQueryMax(vc.getCount());
         Collection<TransferRequest> results = allEQ ? txdao.getAll(vc.getSortType()) : txdao.getByEQ(eqType, vc.getSortType());
         vc.setResults(results);
         
         // Build a set of Pilot IDs
         Collection<Integer> pilotIDs = new HashSet<Integer>();
         for (Iterator<TransferRequest> i = results.iterator(); i.hasNext(); ) {
            TransferRequest txreq = i.next();
            pilotIDs.add(new Integer(txreq.getID()));
         }
         
         // Get equipment types
         GetEquipmentType eqdao = new GetEquipmentType(con);
         ctx.setAttribute("activeEQ", eqdao.getActive(), REQUEST);
         
         // Save the Pilot beans in the request
         GetUserData uddao = new GetUserData(con);
         UserDataMap udm = uddao.get(pilotIDs);
         GetPilot pdao = new GetPilot(con);
         ctx.setAttribute("pilots", pdao.get(udm), REQUEST);
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