//Copyright (c) 2005 James Brickell. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.system.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.Authenticator;
import org.deltava.security.command.*;

import org.deltava.util.PasswordGenerator;
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
	   
	   // Get the command result
	   CommandResult result = ctx.getResult();

		try {
			Connection con = ctx.getConnection();
			
			// Get whichever pilot we're transferring
			GetPilot rdao = new GetPilot(con);
			Pilot p = rdao.get(ctx.getID());
			if (p == null)
				throw new CommandException("Invalid Pilot ID - " + ctx.getID());
			
			// Check access level
			PilotAccessControl access = new PilotAccessControl(ctx,p);
			access.validate();
			if (!access.getCanChangeStatus())
				throw new CommandSecurityException("Insufficient access to transfer a pilot to another airline");
			
			// Save pilot in request
			ctx.setAttribute("pilot", p, REQUEST);
			
			// Get the databases
			GetUserData uddao = new GetUserData(con);
			Map airlines = uddao.getAirlines(false);
			ctx.setAttribute("airlines", airlines.values(), REQUEST);
			
		   // Check if we are transferring or just displaying the JSP
		   if (ctx.getParameter("dbName") == null) {
		      ctx.release();
		      result.setURL("/jsp/pilot/txAirline.jsp");
		      result.setSuccess(true);
		      return;
		   }
			
			// Get the airline to change to
		   AirlineInformation aInfo = (AirlineInformation) airlines.get(ctx.getParameter("dbName"));
		   if (aInfo == null)
		      throw new CommandException("Invalid Airline - " + ctx.getParameter("dbName"));
			
			// Get the equipment types
			GetEquipmentType eqdao = new GetEquipmentType(con);
			Collection eqTypes = eqdao.getActive(aInfo.getDB());
			
			// Check if we've selected an equipmentType/Rank
			if (ctx.getParameter("eqType") == null) {
			   ctx.release();
			   ctx.setAttribute("eqTypes", eqTypes, REQUEST);			   
		      result.setURL("/jsp/pilot/txAirline.jsp");
		      result.setSuccess(true);
		      return;
			}
			
			// Start Transaction
			ctx.startTX();
			
			// Change LDAP DN and assign a new password
			String newDN = "cn=" + p.getName() + ",ou=" + aInfo.getDB().toLowerCase() + ",o=sce";
			p.setPassword(PasswordGenerator.generate(8));
			
			// Create a new UserData record
			UserData ud = new UserData(aInfo.getDB(), "PILOTS", "afva.net");

			// Write the user data record
			SetUserData udao = new SetUserData(con);
			p.setPilotCode("AFV0");
			udao.write(ud);
			
			// Change status at old airline to Transferred
			SetPilot wdao = new SetPilot(con);
			p.setStatus(Pilot.TRANSFERRED);
			wdao.setTransferred(p.getID());
			
			// Get the ID property from the UserData object and stuff it into the existing Pilot object
			p.setID(ud.getID());
			
			// Write the new pilot record in the other database
			wdao.write(p, aInfo.getDB());
			
			// Get the Authenticator
			Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
			
			// Add the new DN to the authenticator with the new password, and remove the old DN
			auth.addUser(newDN, p.getPassword());
			auth.removeUser(p.getDN());
			
			// Commit transaction
			ctx.commitTX();
		} catch (DAOException de) {
		   ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/pilot/pilotTransferred.jsp");
		result.setSuccess(true);
	}
}