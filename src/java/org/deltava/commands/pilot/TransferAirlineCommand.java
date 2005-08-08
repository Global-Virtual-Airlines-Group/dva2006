//Copyright (c) 2005 James Brickell. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.security.command.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A web site command to transfer pilots to a different airline.
 * @author James
 * @version 1.0
 * @since 1.0
 */

public class TransferAirlineCommand extends AbstractCommand {
	/**
     * Execute the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occrurs.
     */
	
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get PID
		int id = (ctx.getID() == 0) ? ctx.getUser().getID() : ctx.getID();
		
		// Get connection from the pool
		try {
			Connection con = ctx.getConnection();
			
			// Get whichever pilot we're transferring
			GetPilot rdao = new GetPilot(con);
			Pilot p = rdao.get(id);
			if (p == null)
				throw new CommandException("Invalid Pilot ID - " + id);
			
			// Check access level
			PilotAccessControl access = new PilotAccessControl(ctx,p);
			access.validate();
			if (!access.getCanChangeStatus())
				throw new CommandSecurityException("Insufficient access to transfer a pilot to another airline");
				
			//TODO Call DAO to Change Pilot's Airline
			
				
			//TODO Change LDAP DN
			
			// Change status at old airline to Transferred
			// Luke - I updated the SetPilot DAO for this - committed also
			SetPilot wdao = new SetPilot(con);
			wdao.setTransferred(p.getID());
			p.setStatus(Pilot.TRANSFERRED);
				
			// Save pilot in request
			ctx.setAttribute("pilot", p, REQUEST);
		} catch (DAOException de) {
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