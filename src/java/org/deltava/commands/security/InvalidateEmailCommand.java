// Copyright 2005, 2006, 2007, 2014, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.security;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.AddressValidation;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.AddressValidationHelper;

/**
 * A Web Site Command to invalidate a user's e-mail address.
 * @author Luke
 * @version 8.7
 * @since 1.0
 */

public class InvalidateEmailCommand extends AbstractCommand {

   /**
    * Execute the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occrurs.
    */
	@Override
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
        	 p.setEmailInvalid(true);
   			addrValid = new AddressValidation(p.getID(), p.getEmail());
   			addrValid.setHash(AddressValidationHelper.calculateHashCode(p.getEmail()));
            addrValid.setInvalid(true);
            
            // Create the status entry
            StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.COMMENT);
            upd.setAuthorID(ctx.getUser().getID());
            upd.setDescription("E-Mail Address Invalidated");
            
            // Start the transaction
            ctx.startTX();
            
            // Write the e-mail address
            SetPilot pwdao = new SetPilot(con);
            pwdao.write(p);
            
            // Write the status update
            SetStatusUpdate updao = new SetStatusUpdate(con);
            updao.write(upd);
            
            // Write the address validation entry
            SetAddressValidation avwdao = new SetAddressValidation(con);
            avwdao.write(addrValid);
            
            // Commit the transaction
            ctx.commitTX();
         } else {
        	 // Check if the old address is still there
        	 if (p.isInvalid()) {
        		 ctx.setAttribute("alreadyInvalid", Boolean.TRUE, REQUEST);        		 
        	 } else {
        		 p.setEmailInvalid(true);
        		 
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