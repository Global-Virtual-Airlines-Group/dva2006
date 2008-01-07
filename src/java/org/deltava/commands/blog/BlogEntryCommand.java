// Copyright 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.blog;

import java.sql.Connection;
import java.util.Collection;

import org.deltava.beans.blog.Entry;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.BlogAccessControl;

/**
 * A Web Site Command to handle blog entries.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class BlogEntryCommand extends AbstractFormCommand {

	/**
	 * Callback method called when saving the blog entry.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execSave(CommandContext ctx) throws CommandException {
		
		// Check if we are creating a new entry
		boolean isNew = (ctx.getID() == 0);
		Entry e = null;
		try {
			Connection con = ctx.getConnection();
			
			// Load the bean if new
			if (!isNew) {
				GetBlog dao = new GetBlog(con);
				e = dao.get(ctx.getID());
				if (e == null)
					throw notFoundException("Invalid Blog entry - " + ctx.getID());
				
				// Check our access
				BlogAccessControl ac = new BlogAccessControl(ctx, e);
				ac.validate();
				if (!ac.getCanEdit())
					throw securityException("Cannot edit blog entry");
				
				e.setTitle(ctx.getParameter("title"));
			} else {
				// Check our access
				BlogAccessControl ac = new BlogAccessControl(ctx, null);
				ac.validate();
				if (!ac.getCanCreate())
					throw securityException("Cannot create blog entry");
				
				e = new Entry(ctx.getParameter("title"));
				e.setAuthorID(ctx.getUser().getID());
			}
			
			// Load fields from request
			e.setBody(ctx.getParameter("body"));
			e.setDate(parseDateTime(ctx, "entry", "MM/dd/yyyy", "HH:mm"));
			e.setLocked(Boolean.valueOf(ctx.getParameter("isLocked")).booleanValue());
			e.setPrivate(Boolean.valueOf(ctx.getParameter("isPrivate")).booleanValue());
			
			// Save the entry
			SetBlog wdao = new SetBlog(con);
			wdao.write(e);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REDIRECT);
		result.setURL("blog", null, e.getAuthorID());
		result.setSuccess(true);
	}

	/**
	 * Callback method called when editing the blog entry.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execEdit(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the blog entry
			Entry e = null;
			if (ctx.getID() != 0) {
				GetBlog dao = new GetBlog(con);
				e = dao.get(ctx.getID());
				if (e == null)
					throw notFoundException("Invalid Blog entry - " + ctx.getID());
			}
				
			// Get our access
			BlogAccessControl ac = new BlogAccessControl(ctx, e);
			ac.validate();
			boolean canExec = (e == null) ? ac.getCanCreate() : ac.getCanEdit();
			if (!canExec)
				throw securityException("Cannot create/edit blog entry");

			// Load the pilot data
			if (e != null) {
				GetPilot pdao = new GetPilot(con);
				ctx.setAttribute("author", pdao.get(e.getAuthorID()), REQUEST);
			} else {
				ctx.setAttribute("author", ctx.getUser(), REQUEST);
			}

			// Save the blog entry in the request
			ctx.setAttribute("entry", e, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/blog/entryEdit.jsp");
		result.setSuccess(true);
	}

	/**
	 * Callback method called when reading the blog entry.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	protected void execRead(CommandContext ctx) throws CommandException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the blog entry
			GetBlog dao = new GetBlog(con);
			Entry e = dao.get(ctx.getID());
			if (e == null)
				throw notFoundException("Invalid Blog entry - " + ctx.getID());
			
			// Get the author IDs
			Collection<Integer> authorIDs = dao.getAuthors(ctx.isUserInRole("Admin"));
			
			// Load the author names
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("authors", pdao.getByID(authorIDs, "PILOTS"), REQUEST);
			
			// Get our access
			BlogAccessControl ac = new BlogAccessControl(ctx, e);
			ac.validate();
			
			// Save the blog entry in the request
			ctx.setAttribute("entry", e, REQUEST);
			ctx.setAttribute("access", ac, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/blog/entryView.jsp");
		result.setSuccess(true);
	}
}