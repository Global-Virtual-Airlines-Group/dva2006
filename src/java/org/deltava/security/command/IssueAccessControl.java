// Copyright 2005, 2006, 2009, 2012, 2016, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.*;
import org.deltava.beans.system.*;

import org.deltava.security.SecurityContext;
import org.deltava.util.system.SystemData;

/**
 * An Access Controller for Issue Tracking.
 * @author Luke
 * @version 9.1
 * @since 1.0
 */

public final class IssueAccessControl extends AccessControl {

	private final Issue _i;

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

	@Override
	public void validate() {
		validateContext();

		// Set issue creation access
		Person p = _ctx.getUser();
		boolean isDev = _ctx.getRoles().contains("Developer");
		boolean isOurAirline = (_i != null) && _i.getAirlines().contains(SystemData.getApp(null));
		boolean isStaff = isDev || _ctx.isUserInRole("PIREP") || _ctx.isUserInRole("Operations") || _ctx.isUserInRole("HR") || _ctx.isUserInRole("Examination") || _ctx.isUserInRole("Instructor");
		boolean hasLegs = (p instanceof Pilot) && (((Pilot) p).getLegs() > 5);
		
		_canCreate = isStaff || hasLegs;
		if (!_ctx.isAuthenticated()) {
			_canRead = (_i == null) ? true : ((_i.getSecurity() == IssueSecurity.PUBLIC) && isOurAirline);
			return;
		}

		// If we're creating a new issue, then don't set the other access control variables
		if (_i == null) {
			_canRead = true;
			_canEdit = _canCreate;
			return;
		}
		
		// Determine state variables
		int userID = _ctx.getUser().getID();
		boolean isOpen = (_i.getStatus() == IssueStatus.OPEN);
		boolean isMine = ((_i.getAuthorID() == userID) || (_i.getAssignedTo() == userID));
		
		// Check read access
		_canRead = (isOurAirline || isDev) && ((_i.getSecurity() == IssueSecurity.STAFF) ? isStaff : true);
		boolean canReopen = (!isOpen && isMine) || isDev;

		// Set access control variables
		_canComment = _ctx.isUserInRole("Pilot") && _canRead && (isOpen || canReopen);
		_canEdit = _canRead && ((isMine && isOpen) || isDev);
		_canResolve = isDev || (isOurAirline && _ctx.isUserInRole("Operations"));
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