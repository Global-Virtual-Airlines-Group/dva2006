// Copyright 2005, 2007, 2008, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.UserDataMap;
import org.deltava.beans.hr.TransferRequest;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to list pending equipment program Transfer Requests.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class TransferListCommand extends AbstractViewCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
   @Override
   public void execute(CommandContext ctx) throws CommandException {
      
      // Initialize the view context
      ViewContext<TransferRequest> vc = initView(ctx, TransferRequest.class);
      String eqType = ctx.getParameter("eqType");
      if (eqType == null)
    	  eqType = ctx.isUserInRole("HR") ? "-" : ctx.getUser().getEquipmentType();
      boolean allEQ = "-".equals(eqType);
      
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the transfer requests
         GetTransferRequest txdao = new GetTransferRequest(con);
         txdao.setQueryStart(vc.getStart());
         txdao.setQueryMax(vc.getCount());
         vc.setResults(allEQ ? txdao.getAll(vc.getSortType()) : txdao.getByEQ(eqType, vc.getSortType()));
         
         // Get equipment types
         GetEquipmentType eqdao = new GetEquipmentType(con);
         ctx.setAttribute("activeEQ", eqdao.getActive(), REQUEST);
         
         // Save the Pilot beans in the request
         Collection<Integer> pilotIDs = vc.getResults().stream().map(TransferRequest::getID).collect(Collectors.toSet());
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