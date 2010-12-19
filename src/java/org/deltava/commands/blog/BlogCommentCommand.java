// Copyright 2006, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.blog;

import java.sql.Connection;

import org.deltava.beans.blog.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.BlogAccessControl;

/**
 * A Web Site Command to save blog entries.
 * @author Luke
 * @version 3.4
 * @since 1.0
 */

public class BlogCommentCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the blog entry
			GetBlog dao = new GetBlog(con);
			Entry e = dao.get(ctx.getID());
			if (e == null)
				throw notFoundException("Invalid Blog entry - " + ctx.getID());

			// Check our access
			BlogAccessControl ac = new BlogAccessControl(ctx, e);
			ac.validate();
			if (!ac.getCanComment())
				throw securityException("Cannot create blog entry comment");

			// Create the comment from the request
			Comment c = new Comment(ctx.getUser().getName(), ctx.getParameter("body"));
			c.setEmail(ctx.getUser().getEmail());
			c.setID(e.getID());
			c.setDate(new java.util.Date());
			c.setRemoteAddr(ctx.getRequest().getRemoteAddr());
			c.setRemoteHost(ctx.getRequest().getRemoteHost());

			// Get the DAO and save the comment
			SetBlog wdao = new SetBlog(con);
			wdao.write(c);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward back to the entry
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("blogentry", "read", ctx.getID());
		result.setSuccess(true);
	}
}