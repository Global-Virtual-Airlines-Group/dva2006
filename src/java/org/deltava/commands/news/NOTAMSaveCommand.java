// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.news;

import java.util.Collection;
import java.sql.Connection;

import org.deltava.beans.*;
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
		
		// Check if we're notifiying users 
		boolean noNotify = Boolean.valueOf(ctx.getParameter("noNotify")).booleanValue();
		Collection<? extends EMailAddress> pilots = null;
		try {
			Connection con = ctx.getConnection();

			// If we're editing, Get the DAO and the existing news entry
			Notice nws = null;
			if (!isNew) {
				GetNews dao = new GetNews(con);
				nws = dao.getNOTAM(ctx.getID());
				if (nws == null)
					throw notFoundException("Invalid NOTAM entry - " + ctx.getID());

				// Check our access
				NewsAccessControl access = new NewsAccessControl(ctx, nws);
				access.validate();
				if (!access.getCanSave())
					throw securityException("Cannot edit NOTAM");

				// Update the entry
				nws.setSubject(ctx.getParameter("subject"));
				nws.setBody(ctx.getParameter("body"));
				nws.setIsHTML(Boolean.valueOf(ctx.getParameter("isHTML")).booleanValue());
				nws.setActive(Boolean.valueOf(ctx.getParameter("active")).booleanValue());
			} else {
				NewsAccessControl access = new NewsAccessControl(ctx, null);
				access.validate();
				if (!access.getCanCreateNOTAM())
					throw securityException("Cannot create NOTAM");

				// Create the news entry
				nws = new Notice(ctx.getParameter("subject"), ctx.getUser().getName(), ctx.getParameter("body"));
				nws.setAuthorID(ctx.getUser().getID());
				nws.setActive(Boolean.valueOf(ctx.getParameter("active")).booleanValue());
				nws.setIsHTML(Boolean.valueOf(ctx.getParameter("isHTML")).booleanValue());
				
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
		if (!noNotify && (pilots != null)) {
			Mailer mailer = new Mailer(ctx.getUser());
			mailer.setContext(mctxt);
			mailer.send(pilots);
			ctx.setAttribute("notifyUsers", new Integer(pilots.size()), REQUEST);
		}

		// Set status attributes
		ctx.setAttribute("isCreate", Boolean.valueOf(isNew), REQUEST);
		ctx.setAttribute("isNOTAM", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/news/newsUpdate.jsp");
		result.setSuccess(true);
	}
}