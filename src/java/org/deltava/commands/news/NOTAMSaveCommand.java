// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.news;

import java.util.List;
import java.sql.Connection;

import org.deltava.beans.Notice;
import org.deltava.beans.Person;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.NewsAccessControl;

/**
 * A Web Site Command to save NOTAM entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class NOTAMSaveCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Check if we're creating a new System News entry
		boolean isNew = (ctx.getID() == 0);

		// Create the Message Context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());

		List pilots = null;
		try {
			Connection con = ctx.getConnection();

			// If we're editing, Get the DAO and the existing news entry
			Notice nws = null;
			if (!isNew) {
				GetNews dao = new GetNews(con);
				nws = dao.getNOTAM(ctx.getID());
				if (nws == null)
					throw new CommandException("Invalid NOTAM entry - " + ctx.getID());

				// Check our access
				NewsAccessControl access = new NewsAccessControl(ctx, nws);
				access.validate();
				if (!access.getCanSave())
					throw new CommandSecurityException("Cannot edit NOTAM");

				// Update the entry
				nws.setSubject(ctx.getParameter("subject"));
				nws.setBody(ctx.getParameter("body"));
				nws.setActive("1".equals(ctx.getParameter("active")));
			} else {
				NewsAccessControl access = new NewsAccessControl(ctx, null);
				access.validate();
				if (!access.getCanCreateNOTAM())
					throw new CommandSecurityException("Cannot create NOTAM");

				// Create the news entry
				nws = new Notice(ctx.getParameter("subject"), ctx.getUser().getName(), ctx.getParameter("body"));
				nws.setAuthorID(ctx.getUser().getID());
				nws.setActive("1".equals(ctx.getParameter("active")));
				
				// Get the message template
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctxt.setTemplate(mtdao.get("NEWNOTAM"));
				mctxt.addData("notam", nws);
				
				// Get the Pilots to send to
				GetPilotNotify pdao = new GetPilotNotify(con);
				pilots = pdao.getNotifications(Person.NEWS);
			}

			// Get the write DAO and save the entry
			SetNews wdao = new SetNews(con);
			wdao.write(nws);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Send the message
		if ((isNew) && (pilots != null)) {
			Mailer mailer = new Mailer(ctx.getUser());
			mailer.setContext(mctxt);
			mailer.send(pilots);
		}

		// Set status attributes
		ctx.setAttribute("isCreate", Boolean.valueOf(isNew), REQUEST);
		ctx.setAttribute("isNOTAM", Boolean.valueOf(true), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/news/newsUpdate.jsp");
		result.setSuccess(true);
	}
}