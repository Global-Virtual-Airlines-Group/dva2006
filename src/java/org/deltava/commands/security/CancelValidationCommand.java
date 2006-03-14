// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.security;

import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.system.AddressValidation;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to cancel an e-mail validation cycle.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CancelValidationCommand extends AbstractCommand {

   /**
    * Execute the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occrurs
    */
   public void execute(CommandContext ctx) throws CommandException {
      
      // Check if we are accepting the new e-mail address or loading another user
      boolean isAccept = ctx.isUserInRole("HR") && (Boolean.valueOf(ctx.getParameter("accept")).booleanValue());
      int id = ctx.isUserInRole("HR") ? ctx.getID() : ctx.getUser().getID();
      if (id == 0)
         id = ctx.getUser().getID();
      
      // Get command result
      CommandResult result = ctx.getResult();
      result.setType(CommandResult.REQREDIRECT);
      
      try {
         Connection con = ctx.getConnection();
         
         // Get the DAO and the address validation bean
         GetAddressValidation avdao = new GetAddressValidation(con);
         AddressValidation av = avdao.get(id);
         if (av == null) {
            ctx.release();
            result.setURL("/jsp/eMailValid.jsp");
            result.setSuccess(true);
            return;
         }
         
         // Get the Pilot
         GetPilot pdao = new GetPilot(con);
         Pilot usr = pdao.get(id);
         if (usr == null)
            throw notFoundException("Invalid Pilot ID - " + id);
         
         // Save user/address validation in request
         ctx.setAttribute("pilot", usr, REQUEST);
         ctx.setAttribute("addr", av, REQUEST);
         
         // Start the transaction
         ctx.startTX();
         
         // If we are accepting the change, update the address
         if (isAccept) {
            usr.setEmail(av.getAddress());
            if (usr.getID() == ctx.getUser().getID())
               ctx.getUser().setEmail(av.getAddress());
            
            // Write the Pilot to the database
            SetPilot pwdao = new SetPilot(con);
            pwdao.write(usr);
         }
         
         // Clear the address validation entry
         SetAddressValidation avwdao = new SetAddressValidation(con);
         avwdao.delete(id);
         
         // Commit the transaction
         ctx.commitTX();
      } catch (DAOException de) {
         ctx.rollbackTX();
         throw new CommandException(de);
      } finally {
         ctx.release();
      }
      
      // Set status attributes
      ctx.setAttribute("isCancel", Boolean.TRUE, REQUEST);
      ctx.setAttribute("forceAccept", Boolean.valueOf(isAccept), REQUEST);

      // Forward to the JSP
      result.setURL("/jsp/eMailValid.jsp");
      result.setSuccess(true);
   }
}