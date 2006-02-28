// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.blog;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.blog.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.BlogAccessControl;

/**
 * A Web Site Command to delete blog comments and entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class BlogDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		Entry e = null;
		boolean doEntry = Boolean.valueOf((String) ctx.getCmdParameter(OPERATION, null)).booleanValue();
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
			if (!ac.getCanDelete())
				throw securityException("Cannot delete blog entry/comment");
			
			// If we're deleting an entry, then nuke the entire thing
			SetBlog wdao = new SetBlog(con);
			if (doEntry) {
				wdao.delete(e.getID());
			} else {
				// Get the timestamp
				long commentID = 0;
				try {
					commentID = Long.parseLong(ctx.getParameter("cID"));
				} catch (Exception ex) {
					CommandException ce = new CommandException("Invalid Comment timestamp - " + ctx.getParameter("cID"));
					ce.setLogStackDump(false);
					throw ce;
				}
				
				// Find the comment
				for (Iterator<Comment> i = e.getComments().iterator(); i.hasNext(); ) {
					Comment c = i.next();
					if (c.getDate().getTime() == commentID) {
						wdao.delete(c);
						break;
					}
				}
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the blog entries
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REDIRECT);
		result.setSuccess(true);
		if (doEntry) {
			result.setURL("blog", null, e.getAuthorID());
		} else {
			result.setURL("blogentry", "read", e.getID());
		}
	}
}