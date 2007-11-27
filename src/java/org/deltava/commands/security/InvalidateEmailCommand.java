// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.security;

import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.EMailAddress;
import org.deltava.beans.system.AddressValidation;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to invalidate a user's e-mail address.
 * @author Luke
 * @version 2.0
 * @since 1.0
 */

public class InvalidateEmailCommand extends AbstractCommand {

   /**
    * Execute the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occrurs.
    */
   public void execute(CommandContext ctx) throws CommandException {

      try {
         Connection con = ctx.getConnection();
         
         // Get the Pilot
         GetPilot dao = new GetPilot(con);
         Pilot p = dao.get(ctx.getID());
         if (p == null)
            throw notFoundException("Invalid Pilot - " + ctx.getID());
         
         // Check if the address has already been invalidated
         GetAddressValidation avdao = new GetAddressValidation(con);
         AddressValidation addrValid = avdao.get(p.getID());
         if (addrValid == null) {
            // Invalidate the e-mail address and create the validation entry
            p.setEmail(EMailAddress.INVALID_ADDR);
   			addrValid = new AddressValidation(p.getID(), EMailAddress.INVALID_ADDR);
            addrValid.setHash(EMailAddress.INVALID_ADDR);
            
            // Start the transaction
            ctx.startTX();
            
            // Write the e-mail address
            SetPilot pwdao = new SetPilot(con);
            pwdao.write(p);
            
            // Write the address validation entry
            SetAddressValidation avwdao = new SetAddressValidation(con);
            avwdao.write(addrValid);
            
            // Commit the transaction
            ctx.commitTX();
         } else {
        	 // Check if the old address is still there
        	 if (EMailAddress.INVALID_ADDR.equals(p.getEmail())) {
        		 ctx.setAttribute("alreadyInvalid", Boolean.TRUE, REQUEST);        		 
        	 } else {
        		 p.setEmail(EMailAddress.INVALID_ADDR);
        		 
                 // Write the e-mail address
                 SetPilot pwdao = new SetPilot(con);
                 pwdao.write(p);
        	 }
         }
         
         // Save the pilot in the request
         ctx.setAttribute("pilot", p, REQUEST);
      } catch (DAOException de) {
         ctx.rollbackTX();
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/pilot/eMailInvalidate.jsp");
      result.setSuccess(true);
   }
}