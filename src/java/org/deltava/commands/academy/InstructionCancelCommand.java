// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.academy.InstructionSession;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.InstructionAccessControl;

/**
 * A Web Site Command to cancel a Flight Academy Instruction Session.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InstructionCancelCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Init the message context
		MessageContext mctx = new MessageContext();
		mctx.addData("user", ctx.getUser());

		Collection<EMailAddress> usrs = new HashSet<EMailAddress>();
		try {
			Connection con = ctx.getConnection();
			
			// Get the session data
			GetAcademyCalendar dao = new GetAcademyCalendar(con);
			InstructionSession s = dao.getSession(ctx.getID());
			if (s == null)
				throw notFoundException("Invalid Instruction Session - " + ctx.getID());
			
			// Check our access
			InstructionAccessControl access = new InstructionAccessControl(ctx, s);
			access.validate();
			if (!access.getCanCancel())
				throw securityException("Cannot cancel Instruction Session");
			
			// Load the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctx.setTemplate(mtdao.get("INSCANCEL"));
			
			// Figure out the recipients
			Collection<Integer> IDs = new HashSet<Integer>();
			IDs.add(new Integer(s.getInstructorID()));
			IDs.add(new Integer(s.getPilotID()));
			IDs.remove(new Integer(ctx.getUser().getID()));
			
			// Load recipient profiles
			GetPilot pdao = new GetPilot(con);
			Map<Integer, Pilot> pilots = pdao.getByID(IDs, "PILOTS"); 
			usrs.addAll(pilots.values());
			
			// Update the message context
			mctx.addData("session", s);
			ctx.setAttribute("session", s, REQUEST);
			ctx.setAttribute("pilots", pilots, REQUEST);
			
			// Update the session
			s.setStatus(InstructionSession.CANCELED);
			
			// Save the session
			SetAcademyCalendar wdao = new SetAcademyCalendar(con);
			wdao.write(s);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attribute
		ctx.setAttribute("isSessionCancel", Boolean.TRUE, REQUEST);
		
		// Send the message
		Mailer mailer = new Mailer(ctx.getUser());
		mailer.setContext(mctx);
		mailer.send(usrs);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/courseUpdate.jsp");
		result.setType(CommandResult.REQREDIRECT);
		result.setSuccess(true);
	}
}