//Copyright (c) 2005 James Brickell. All Rights Reserved.
package org.deltava.commands.pilot;

import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.security.command.*;

import org.deltava.util.system.SystemData;

/**
 * A web site command to transfer pilots to a different airline.
 * @author James
 * @version 1.0
 * @since 1.0
 */

public class TransferAirlineCommand extends AbstractCommand {
	
   public static final Logger log = Logger.getLogger(TransferAirlineCommand.class);
   
   /**
     * Execute the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occrurs.
     */
	
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get PID
	   // FIXME just use ctx.getID() - if none is provided your code will transfer the current pilot.
		int id = (ctx.getID() == 0) ? ctx.getUser().getID() : ctx.getID();
		
		// Read a request parameter called "newAirline" to get the airline to change to
		
		// Get the airlineDatabases Map from SystemData and make sure the db name is contained
		// within map.keySet() if not, throw an exception
		
		// Get connection from the pool
		try {
			Connection con = ctx.getConnection();
			
			// Get whichever pilot we're transferring
			GetPilot rdao = new GetPilot(con);
			Pilot p = rdao.get(id); // call ctx.getID() here
			if (p == null)
				throw new CommandException("Invalid Pilot ID - " + id);
			
			// Check access level
			PilotAccessControl access = new PilotAccessControl(ctx,p);
			access.validate();
			if (!access.getCanChangeStatus())
				throw new CommandSecurityException("Insufficient access to transfer a pilot to another airline");
			
			// Start Transaction
				
			//TODO Change LDAP DN
			// What I think we should do here is build the DN as cn= + p.getName() + ",ou=" + dbName + ",o=sce"
			// Let's hard code it for now, and figure out the elegant way later
			
			// Create a new userData record and write it
			
			
			// Change status at old airline to Transferred
			SetPilot wdao = new SetPilot(con);
			p.setStatus(Pilot.TRANSFERRED);
			wdao.setTransferred(p.getID());
			
			// Get the ID property from the UserData object and stuff it into the existing Pilot object
			
			// Write the new pilot record in the other database
			
			// Get the Authenticator
			
			// Calculate a random password, since we can't read the old password
			
			// Add the new DN to the authenticator with the new password
			
			// Remove the old DN
			
			// Commit transaction
			
			
			// Save pilot in request
			ctx.setAttribute("pilot", p, REQUEST);
		} catch (DAOException de) {
		   // Rollback transaction
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		//Forward to JSP
		//TODO Create JSP for Transfer Airline
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/pilot/pilotTransferred.jsp");
		result.setSuccess(true);
	}
}