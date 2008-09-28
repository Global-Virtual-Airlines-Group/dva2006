// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.blog;

import java.sql.Connection;

import org.deltava.beans.blog.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.BlogAccessControl;

/**
 * A Web Site Command to save blog entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class BlogCommentCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		Entry e = null;
		try {
			Connection con = ctx.getConnection();

			// Get the DAO and the blog entry
			GetBlog dao = new GetBlog(con);
			e = dao.get(ctx.getID());
			if (e == null)
				throw notFoundException("Invalid Blog entry - " + ctx.getID());

			// Check our access
			BlogAccessControl ac = new BlogAccessControl(ctx, e);
			ac.validate();
			if (!ac.getCanComment())
				throw securityException("Cannot create blog entry comment");

			// Create the comment from the request
			String name = ctx.isAuthenticated() ? ctx.getUser().getName() : ctx.getParameter("name");
			Comment c = new Comment(name, ctx.getParameter("body"));
			c.setEmail(ctx.isAuthenticated() ? ctx.getUser().getEmail() : ctx.getParameter("eMail"));
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
		result.setURL("blogentry", "read", e.getID());
		result.setSuccess(true);
	}
}