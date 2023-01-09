// Copyright 2005, 2006, 2011, 2015, 2019, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.news;

import java.util.List;
import java.sql.Connection;

import org.deltava.beans.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.mail.*;

import org.deltava.security.command.NewsAccessControl;

/**
 * A Web Site Command to save System News entries.
 * @author Luke
 * @version 10.4
 * @since 1.0
 */

public class NewsSaveCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Check if we're creating a new System News entry
		boolean isNew = (ctx.getID() == 0);

		// Create the Message Context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());

		List<? extends EMailAddress> pilots = null;
		try {
			Connection con = ctx.getConnection();

			// If we're editing, Get the DAO and the existing news entry
			News nws = null;
			if (!isNew) {
				GetNews dao = new GetNews(con);
				nws = dao.getNews(ctx.getID());
				if (nws == null)
					throw notFoundException("Invalid System News entry - " + ctx.getID());

				// Check our access
				NewsAccessControl access = new NewsAccessControl(ctx, nws);
				access.validate();
				if (!access.getCanSave())
					throw securityException("Cannot edit System News entry");

				// Update the entry
				nws.setSubject(ctx.getParameter("subject"));
				nws.setBody(ctx.getParameter("body"));
				nws.setIsHTML(Boolean.parseBoolean(ctx.getParameter("isHTML")));
			} else {
				NewsAccessControl access = new NewsAccessControl(ctx, null);
				access.validate();
				if (!access.getCanCreateNews())
					throw securityException("Cannot edit System News entry");

				// Create the news entry
				nws = new News(ctx.getParameter("subject"), ctx.getParameter("body"));
				nws.setAuthorID(ctx.getUser().getID());
				nws.setIsHTML(Boolean.parseBoolean(ctx.getParameter("isHTML")));

				// Get the message template
				GetMessageTemplate mtdao = new GetMessageTemplate(con);
				mctxt.setTemplate(mtdao.get("NEWNEWS"));
				mctxt.addData("news", nws);
				
				// Get the pilots to notify
				GetPilotNotify pdao = new GetPilotNotify(con);
				pilots = pdao.getNotifications(Notification.NEWS);
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
		if (isNew && (pilots != null)) {
			Mailer mailer = new Mailer(ctx.getUser());
			mailer.setContext(mctxt);
			mailer.send(pilots);
			ctx.setAttribute("notifyUsers", Integer.valueOf(pilots.size()), REQUEST);
		}
		
		// Set status attributes
		ctx.setAttribute("isCreate", Boolean.valueOf(isNew), REQUEST);
		ctx.setAttribute("isNews", Boolean.TRUE, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/news/newsUpdate.jsp");
		result.setSuccess(true);
	}
}