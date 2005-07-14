// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.admin;

import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.system.TransferRequest;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.TransferAccessControl;

/**
 * A Web Site Command to display a Transfer Request for processing.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TransferProcessCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      try {
         Connection con = ctx.getConnection();

         // Get the DAO and the transfer request
         GetTransferRequest txdao = new GetTransferRequest(con);
         TransferRequest txreq = txdao.get(ctx.getID());
         if (txreq == null) throw new CommandException("Invalid Transfer Request - " + ctx.getID());

         // Check our access
         TransferAccessControl access = new TransferAccessControl(ctx, txreq);
         access.validate();

         // Get the pilot
         GetPilot pdao = new GetPilot(con);
         Pilot usr = pdao.get(txreq.getID());
         ctx.setAttribute("pilot", usr, REQUEST);
         
         // Get the requested equipment type
         GetEquipmentType eqdao = new GetEquipmentType(con);
         ctx.setAttribute("activeEQ", eqdao.getActive(), REQUEST);
         ctx.setAttribute("eqType", eqdao.get(txreq.getEquipmentType()), REQUEST);
         ctx.setAttribute("currentEQ", eqdao.get(usr.getEquipmentType()), REQUEST);

         // See if there's a checkride
         if (txreq.getCheckRideID() != 0) {
            GetExam exdao = new GetExam(con);
            ctx.setAttribute("checkRide", exdao.getCheckRide(txreq.getCheckRideID()), REQUEST);
         }
         
         // Save the transfer request and access controller
         ctx.setAttribute("txReq", txreq, REQUEST);
         ctx.setAttribute("access", access, REQUEST);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/admin/txRequestProcess.jsp");
      result.setSuccess(true);
   }
}