// Copyright 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.blog;

import java.util.*;
import java.sql.Connection;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.BlogAccessControl;

/**
 * A Web Site Command to display a blog.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class BlogCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get the view context
		ViewContext vc = initView(ctx, 5);
		
		// Check if we can see private entries
		boolean showAll = (ctx.getID() == 0);
		boolean showPrivate = ctx.isUserInRole("Admin") || (ctx.isAuthenticated() && (ctx.getUser().getID() == ctx.getID()));
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the authors
			GetBlog dao = new GetBlog(con);
			Collection<Integer> authorIDs = dao.getAuthors(showPrivate);
			if (!showAll && (!authorIDs.contains(new Integer(ctx.getID()))))
				throw notFoundException("Invalid Author ID - " + ctx.getID());
			
			// Get the blog entries
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(dao.getLatest(ctx.getID(), showPrivate));
			
			// Load the author names
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("authors", pdao.getByID(authorIDs, "PILOTS"), REQUEST);
			if (!showAll)
				ctx.setAttribute("authorID", new Integer(ctx.getID()), REQUEST);
			
			// Get our access
			BlogAccessControl ac = new BlogAccessControl(ctx, null);
			ac.validate();
			ctx.setAttribute("access", ac, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save display attributes
		ctx.setAttribute("showAll", Boolean.valueOf(showAll), REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/blog/blogList.jsp");
		result.setSuccess(true);
	}
}