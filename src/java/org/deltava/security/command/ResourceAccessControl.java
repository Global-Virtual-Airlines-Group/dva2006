// Copyright 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.fleet.Resource;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Web Resources.
 * @author Luke
 * @version 2.7
 * @since 1.0
 */

public class ResourceAccessControl extends AccessControl {

	private Resource _r;
	private boolean _canCreate;
	private boolean _canEdit;
	private boolean _canDelete;

	/**
	 * Initializes the Access Controller.
	 * @param ctx the Security context
	 * @param r the Web Resource
	 */
	public ResourceAccessControl(SecurityContext ctx, Resource r) {
		super(ctx);
		_r = r;
	}

	/**
	 * Calculates Access roles.
	 */
	public void validate() {
		validateContext();

		// Check create access
		_canCreate = _ctx.isUserInRole("Pilot");
		if (_r == null)
			return;

		// Validate edit/delete rights
		boolean isMine = (_r.getAuthorID() == _ctx.getUser().getID());
		boolean isAdmin = _ctx.isUserInRole("Resource") || _ctx.isUserInRole("HR") || _ctx.isUserInRole("Fleet");
		_canEdit = isAdmin || (isMine && !_r.getPublic());
		_canDelete = isAdmin;
	}

	/**
	 * Returns wether a new Web Resource can be created.
	 * @return TRUE if a Resource can be created, otherwise FALSE
	 */
	public boolean getCanCreate() {
		return _canCreate;
	}

	/**
	 * Returns wether the Web Resource can be edited.
	 * @return TRUE if the Resource can be edited, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}

	/**
	 * Returns wether the Web Resource can be deleted.
	 * @return TRUE if the Resource can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}