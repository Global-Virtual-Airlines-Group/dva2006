// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.StatusUpdate;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.Authenticator;
import org.deltava.security.command.PilotAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to merge two pilot profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class DuplicatePilotMergeCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the pilot IDs to merge
		Collection<Integer> ids = new HashSet<Integer>();
		Collection<String> mergeIDs = ctx.getParameters("sourceID");
		for (Iterator<String> i = mergeIDs.iterator(); i.hasNext();)
			ids.add(new Integer(StringUtils.parseHex(i.next())));
		
		// Create the messaging context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());
		
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the pilots
			GetPilot dao = new GetPilot(con);
			Collection<Pilot> src = dao.getByID(ids, "PILOTS").values();
			Pilot usr = dao.get(ctx.getID());
			if (usr == null)
				throw new CommandException("Invalid User - " + ctx.getID());

			// Validate our access
			for (Iterator i = src.iterator(); i.hasNext();) {
				Pilot p = (Pilot) i.next();
				PilotAccessControl access = new PilotAccessControl(ctx, p);
				access.validate();
				if (!access.getCanChangeStatus())
					i.remove();
			}

			// Check that we can merge any pilots
			if (src.isEmpty())
				throw securityException("Cannot merge Pilots");

			// Start a JDBC transaction
			ctx.startTX();
			
			// Get the roles
			Collection<String> newRoles = new HashSet<String>(usr.getRoles()); 

			// Iterate through the Pilots, combining the roles
			Collection<StatusUpdate> sUpdates = new ArrayList<StatusUpdate>();
			SetPilotMerge mgdao = new SetPilotMerge(con);
			for (Iterator<Pilot> i = src.iterator(); i.hasNext();) {
				Pilot p = i.next();
				newRoles.addAll(p.getRoles());

				// Create a status update
				if (p.getID() != usr.getID()) {
					StatusUpdate su = new StatusUpdate(p.getID(), StatusUpdate.STATUS_CHANGE);
					su.setAuthorID(ctx.getUser().getID());
					su.setDescription("Merged into " + usr.getName() + " (" + usr.getPilotCode() + ")");
					sUpdates.add(su);

					// Migrate the data
					mgdao.merge(p, usr);
				}
			}
			
			// Update the roles and status
			boolean updatePassword = (usr.getStatus() != Pilot.ACTIVE);
			usr.addRoles(newRoles);
			usr.setStatus(Pilot.ACTIVE);
			
			// Write the pilot profile
			SetPilot pwdao = new SetPilot(con);
			pwdao.write(usr);

			// Write status updates
			SetStatusUpdate sudao = new SetStatusUpdate(con);
			sudao.write(sUpdates);
			
			// If we're not active, generate a new password
			if (updatePassword) {
				String newPwd = PasswordGenerator.generate(10);
			
				// Get the authenticator and update the password
				Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
				if (auth.contains(usr)) {
					auth.updatePassword(usr, newPwd);
				} else {
					auth.addUser(usr, newPwd);
				}
			
				// Get the message template
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctxt.setTemplate(mtdao.get("PWDRESET"));
				
				// Send a notification message
				Mailer mailer = new Mailer(ctx.getUser());
				mailer.setContext(mctxt);
				mailer.send(usr);
			}

			// Commit the transaction
			ctx.commitTX();

			// Save the pilots
			ctx.setAttribute("pilot", usr, REQUEST);
			ctx.setAttribute("oldPilots", src, REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/roster/dupeMerge.jsp");
		result.setSuccess(true);
	}
}