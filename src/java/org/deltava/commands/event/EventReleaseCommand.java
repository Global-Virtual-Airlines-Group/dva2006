// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.event;

import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.event.*;
import org.deltava.beans.system.UserData;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.SignupAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to release an Online Event Signup.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EventReleaseCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the Pilot ID
		int pilotID = StringUtils.parseHex((String) ctx.getCmdParameter(Command.OPERATION, null));
		
		// Create the messaging context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());

		Pilot usr = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the event
			GetEvent dao = new GetEvent(con);
			Event e = dao.get(ctx.getID());
			if (e == null)
				throw notFoundException("Invalid Online Event - " + ctx.getID());
			
			// Get the Signup for the Pilot
			Signup s = e.getSignup(pilotID);
			if (s == null)
				throw notFoundException("Pilot " + pilotID + " not signed up for Online Event - " + ctx.getID());
			
			// Get the access controller
			SignupAccessControl access = new SignupAccessControl(ctx, e, s);
			access.validate();
			if (!access.getCanRelease())
				throw securityException("Cannot release Online Event Signup");
			
			// Get the Pilot location
			GetUserData usrdao = new GetUserData(con);
			UserData ud = usrdao.get(pilotID);
			
			// Get the Pilot
			GetPilot pdao = new GetPilot(con);
			usr = pdao.get(ud);
			
			// Get the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("EVENTRELEASE")); 
			
			// Get the write DAO
			SetEvent wdao = new SetEvent(con);
			wdao.delete(s);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Send notification e-mail
		Mailer mailer = new Mailer(ctx.getUser());
		mailer.setContext(mctxt);
		mailer.send(usr);
		
		// Forward back to the event
		CommandResult result = ctx.getResult();
		result.setURL("event", null, ctx.getID());
		result.setSuccess(true);
	}
}