// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.blog.Entry;

import org.deltava.commands.CommandException;
import org.deltava.security.SecurityContext;

/**
 * An Access Controller for blog entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class BlogAccessControl extends AccessControl {
	
	private Entry _e;
	
	private boolean _canCreate;
	private boolean _canEdit;
	private boolean _canComment;
	private boolean _canDelete;

	/**
	 * Initialize the Access Controller.
	 * @param ctx the security context
	 * @param e the blog Entry bean
	 */
	public BlogAccessControl(SecurityContext ctx, Entry e) {
		super(ctx);
		_e = e;
	}

	/**
	 * Calculates access roles.
	 * @throws CommandException if the entry cannot be read
	 */
	public void validate() throws CommandException {
		validateContext();
		
		// If no entry, then validate create access
		_canCreate = _ctx.isUserInRole("Blog");
		if (_e == null)
			return;

		// Check our read access if private
		int id = _ctx.isAuthenticated() ? _ctx.getUser().getID() : 0;
		if (_e.getPrivate() && (id != _e.getAuthorID()))
			throw new AccessControlException("Cannot view blog entry");
		
		// Get access roles
		_canEdit = (_ctx.isAuthenticated() && (id == _e.getAuthorID())) || _ctx.isUserInRole("Admin");
		_canComment = _ctx.isAuthenticated() || (!_e.getLocked());
		_canDelete = _canEdit;
	}

	/**
	 * Returns wether the user can create a new blog entry.
	 * @return TRUE if an entry can be created, otherwise FALSE
	 */
	public boolean getCanCreate() {
		return _canCreate;
	}
	
	/**
	 * Returns wether the user can edit the current blog entry.
	 * @return TRUE if the entry can be edited, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}
	
	/**
	 * Returns wether the user can comment on the current blog entry.
	 * @return TRUE if the entry can be commented on, otherwise FALSE
	 */
	public boolean getCanComment() {
		return _canComment;
	}
	
	/**
	 * Returns wether the user can delete the current blog entry.
	 * @return TRUE if the entry can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}