//Copyright (c) 2005 James Brickell. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.system.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

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
	   
	   // Initialize the Message context
	   MessageContext mctxt = new MessageContext();
	   mctxt.addData("user", ctx.getUser());

	   Pilot newUser = null;
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
			
			// Get the Message Template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("TXAIRLINE"));
			
			// Start Transaction
			ctx.startTX();
			
			// Get the Pilot write DAO
			SetPilot wdao = new SetPilot(con);
			
			// Check if the user already exists in the database
			newUser = rdao.getByName(p.getName());
			if (newUser != null) {
			   log.info("Reactivating " + newUser.getDN());
			} else {
			   log.info("Creating User record for " + p.getName() + " at " + aInfo.getCode());
			   
			   // Clone the Pilot and update the ID
			   newUser = p.cloneExceptID();
			   p.setPilotCode(aInfo.getCode() + "0");
			   
				// Change LDAP DN and assign a new password
				newUser.setDN("cn=" + p.getName() + ",ou=" + aInfo.getDB().toLowerCase() + ",o=sce");

				// Create a new UserData record
				UserData ud = new UserData(aInfo.getDB(), "PILOTS", aInfo.getDomain());
				
				// Write the user data record
				SetUserData udao = new SetUserData(con);
				udao.write(ud);

				// Get the ID property from the UserData object and stuff it into the existing Pilot object
				newUser.setID(ud.getID());
			}
			
			// Change status at old airline to Transferred
			p.setStatus(Pilot.TRANSFERRED);
			wdao.setTransferred(p.getID());
			
			// Save the new user
			newUser.setStatus(Pilot.ACTIVE);
			wdao.write(newUser, aInfo.getDB());
			
			// Add the new DN to the authenticator with the new password, and remove the old DN
			Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
			newUser.setPassword(PasswordGenerator.generate(8));
			auth.addUser(newUser.getDN(), newUser.getPassword());
			
			// Commit transaction
			ctx.commitTX();
			
			// Update the message context
			mctxt.addData("oldUser", p);
			mctxt.addData("newUser", newUser);
			
			// Save Pilot beans in the request
			ctx.setAttribute("oldUser", p, REQUEST);
			ctx.setAttribute("newUser", newUser, REQUEST);
		} catch (DAOException de) {
		   ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Send status e-mail
		Mailer mailer = new Mailer(ctx.getUser());
		mailer.setContext(mctxt);
		mailer.send(newUser);
		
		// Forward to the JSP
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/pilot/pilotTransferred.jsp");
		result.setSuccess(true);
	}
}