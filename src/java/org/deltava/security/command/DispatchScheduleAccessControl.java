// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.acars.DispatchScheduleEntry;

import org.deltava.security.SecurityContext;

/**
 * An access controller for ACARS Dispatcher service entries.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class DispatchScheduleAccessControl extends AccessControl {

	private DispatchScheduleEntry _dse;
	
	private boolean _canCreate;
	private boolean _canDelete;
	private boolean _canEdit;
	
	/**
	 * Initializes the access controller.
	 * @param ctx the security context
	 * @param dse the DispatchScheduleEntry bean, or null
	 */
	public DispatchScheduleAccessControl(SecurityContext ctx, DispatchScheduleEntry dse) {
		super(ctx);
		_dse = dse;
	}

	/**
	 * Calculates access rights.
	 */
	public void validate() {
		validateContext();
		
		// Check access
		boolean isDispatch = _ctx.isUserInRole("Dispatch");
		boolean isOurs = (_dse != null) && _ctx.isAuthenticated() && (_dse.getAuthorID() == _ctx.getUser().getID());
		_canCreate = _ctx.isUserInRole("Dispatch") || _ctx.isUserInRole("HR");
		_canEdit = (isOurs && isDispatch) || _ctx.isUserInRole("HR");
		_canDelete = (isOurs && isDispatch && (_dse.getEndTime().getTime() > System.currentTimeMillis())) || _ctx.isUserInRole("HR");
	}

	/**
	 * Returns whether a new schedule entry can be created.
	 * @return TRUE if a schedule entry can be created, otherwise FALSE
	 */
	public boolean getCanCreate() {
		return _canCreate;
	}
	
	/**
	 * Returns whether the current schedule entry can be edited.
	 * @return TRUE if the entry can be edited, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}
	
	/**
	 * Returns whether the current schedule entry can be deleted.
	 * @return TRUE if the entry can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}