// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.system.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.*;
import org.deltava.security.command.*;

import org.deltava.util.PasswordGenerator;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to transfer pilots to a different airline.
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
				throw notFoundException("Invalid Pilot ID - " + ctx.getID());
			
			// Check access level
			PilotAccessControl access = new PilotAccessControl(ctx, p);
			access.validate();
			if (!access.getCanChangeStatus())
				throw securityException("Insufficient access to transfer a pilot to another airline");

			// Save pilot in request
			ctx.setAttribute("pilot", p, REQUEST);

			// Get the databases
			GetUserData uddao = new GetUserData(con);
			Map<String, AirlineInformation> airlines = uddao.getAirlines(false);
			ctx.setAttribute("airlines", airlines.values(), REQUEST);

			// Check if we are transferring or just displaying the JSP
			if (ctx.getParameter("dbName") == null) {
				ctx.release();
				result.setURL("/jsp/pilot/txAirline.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Get the airline to change to
			AirlineInformation aInfo = airlines.get(ctx.getParameter("dbName"));
			if (aInfo == null)
				throw notFoundException("Invalid Airline - " + ctx.getParameter("dbName"));

			// Get the equipment types
			GetEquipmentType eqdao = new GetEquipmentType(con);
			Collection<EquipmentType> eqTypes = eqdao.getActive(aInfo.getDB());

			// Check if we've selected an equipmentType/Rank
			if (ctx.getParameter("eqType") == null) {
				ctx.release();
				ctx.setAttribute("eqTypes", eqTypes, REQUEST);
				result.setURL("/jsp/pilot/txAirline.jsp");
				result.setSuccess(true);
				return;
			}
			
			// Load the user's cross-airline information and see if the user already exists
			UserData ud = uddao.get(p.getID());
			UserDataMap udm = uddao.get(ud.getIDs());
			boolean isExisting = udm.getTableNames().contains(aInfo.getDB() + ".PILOTS");
			
			// Get the Message Template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("TXAIRLINE"));

			// Add equipment ratings
			p.addRatings(eqdao.getPrimaryTypes(aInfo.getDB(), ctx.getParameter("eqType")));

			// Start Transaction
			ctx.startTX();

			// Get the Pilot/Status Update write DAOs
			SetPilotTransfer wdao = new SetPilotTransfer(con);
			SetStatusUpdate sudao = new SetStatusUpdate(con);

			// Create the status update
			StatusUpdate su = new StatusUpdate(p.getID(), StatusUpdate.AIRLINE_TX);
			su.setAuthorID(ctx.getUser().getID());
			su.setDescription("Transferred to " + aInfo.getName());
			sudao.write(su);

			// Check if the user already exists in the new airline database
			if (isExisting) {
				Collection<UserData> users = udm.getByTable(aInfo.getDB() + ".PILOTS");
				if (!users.isEmpty()) {
					UserData ud2 = users.iterator().next();
					newUser = rdao.get(ud2);
				}
			}
			
			if (newUser != null)
				log.info("Reactivating " + newUser.getName());
			else {
				log.info("Creating User record for " + p.getName() + " at " + aInfo.getCode());

				// Clone the Pilot and update the ID
				newUser = p.cloneExceptID();
				p.setPilotCode(aInfo.getCode() + "0");

				// Change LDAP DN and assign a new password
				newUser.setDN("cn=" + p.getName() + ",ou=" + aInfo.getDB().toLowerCase() + ",o=sce");

				// Create a new UserData record
				ud = new UserData(aInfo.getDB(), "PILOTS", aInfo.getDomain());
				ud.addID(p.getID());

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
			newUser.setEquipmentType(ctx.getParameter("eqType"));
			newUser.setRank(ctx.getParameter("rank"));
			if (!isExisting) {
				wdao.transfer(newUser, aInfo.getDB(), newUser.getRatings());
				
				// Assign an ID if requested
				boolean assignID = Boolean.valueOf(ctx.getParameter("assignID")).booleanValue();
				if (assignID)
					wdao.assignID(newUser, aInfo.getDB());
			} else
				wdao.write(newUser, aInfo.getDB());

			// Create the second status update
			StatusUpdate su2 = new StatusUpdate(newUser.getID(), StatusUpdate.AIRLINE_TX);
			su2.setAuthorID(ctx.getUser().getID());
			su2.setDescription("Transferred from " + SystemData.get("airline.name"));
			sudao.write(aInfo.getDB(), su2);
			
			// Calculate the new password
			newUser.setPassword(PasswordGenerator.generate(SystemData.getInt("security.password.default", 8)));
			
			// Add the new DN to the authenticator with the new password, and remove the old DN
			Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
			if (auth instanceof SQLAuthenticator)
				((SQLAuthenticator) auth).setConnection(con);
			
			if (auth.contains(newUser))
				auth.updatePassword(newUser, newUser.getPassword());
			else
				auth.add(newUser, newUser.getPassword());
			
			// Reset the authenticator
			if (auth instanceof SQLAuthenticator)
				((SQLAuthenticator) auth).clearConnection();
			
			// Commit transaction
			ctx.commitTX();

			// Update the message context
			mctxt.addData("oldUser", p);
			mctxt.addData("newUser", newUser);
			mctxt.addData("newAirline", aInfo);

			// Save Pilot beans in the request
			ctx.setAttribute("oldUser", p, REQUEST);
			ctx.setAttribute("newUser", newUser, REQUEST);
			ctx.setAttribute("airline", aInfo, REQUEST);
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
		mailer.setCC(ctx.getUser());

		// Forward to the JSP
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/pilot/pilotTransferred.jsp");
		result.setSuccess(true);
	}
}