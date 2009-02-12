// Copyright 2005, 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;

import org.deltava.beans.system.Issue;

/**
 * An Access Controller for Issue Tracking.
 * @author Luke
 * @version 2.4
 * @since 1.0
 */

public final class IssueAccessControl extends AccessControl {

	private Issue _i;

	private boolean _canCreate;
	private boolean _canRead;
	private boolean _canComment;
	private boolean _canEdit;
	private boolean _canReassign;
	private boolean _canResolve;

	/**
	 * Creates a new Access Controller.
	 * @param ctx the Command context
	 * @param i the Issue
	 */
	public IssueAccessControl(SecurityContext ctx, Issue i) {
		super(ctx);
		_i = i;
	}

	/**
	 * Calculates access rights.
	 */
	public void validate() {
		validateContext();

		// Set issue creation access
		_canCreate = _ctx.isUserInRole("Pilot");
		if (!_ctx.isAuthenticated()) {
			_canRead = (_i == null) ? true : (_i.getSecurity() == Issue.SECURITY_PUBLIC);
			return;
		}

		// If we're creating a new issue, then don't set the other access control variables
		if (_i == null) {
			_canRead = true;
			_canEdit = _canCreate;
			_canReassign = _ctx.isUserInRole("Developer");
			return;
		}

		// Determine state variables
		int userID = _ctx.getUser().getID();
		boolean isOpen = (_i.getStatus() == Issue.STATUS_OPEN);
		boolean isMine = ((_i.getAuthorID() == userID) || (_i.getAssignedTo() == userID));
		boolean isDev = _ctx.isUserInRole("Developer");
		boolean isStaff = _ctx.isUserInRole("HR") || _ctx.isUserInRole("PIREP") || _ctx.isUserInRole("Examination");
		
		// Check read access
		_canRead = (_i.getSecurity() == Issue.SECURITY_STAFF) ? isStaff || isDev : true;

		// Set access control variables
		_canComment = _canCreate && _canRead && (isOpen || isDev); // This locks out comments for closed issues by non-devs
		_canEdit = _canRead && ((isMine && isOpen) || isDev);
		_canResolve = isDev;
		_canReassign = isDev && isOpen;
	}

	/**
	 * Returns if a new Issue can be created.
	 * @return TRUE if a new Issue can be created, otherwise FALSE
	 */
	public boolean getCanCreate() {
		return _canCreate;
	}

	/**
	 * Returns if the Issue can be read.
	 * @return TRUE if the user can read the Issue, otherwise FALSE
	 */
	public boolean getCanRead() {
		return _canRead;
	}

	/**
	 * Returns if a new Issue Comment can be created.
	 * @return TRUE if a new Issue Comment can be created, otherwise FALSE
	 */
	public boolean getCanComment() {
		return _canComment;
	}

	/**
	 * Returns if the Issue can be Edited.
	 * @return TRUE if the Issue can be edited, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}

	/**
	 * Returns if the Issue can be Resolved.
	 * @return TRUE if the Issue can be resolved, otherwise FALSE
	 */
	public boolean getCanResolve() {
		return _canResolve;
	}

	/**
	 * Returns if the Issue can be Reassigned.
	 * @return TRUE if the Issue can be reassigned, otherwise FALSE
	 */
	public boolean getCanReassign() {
		return _canReassign;
	}
}