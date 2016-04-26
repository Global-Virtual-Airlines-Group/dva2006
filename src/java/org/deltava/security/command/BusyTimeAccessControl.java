// Copyright 2007, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.academy.InstructionBusy;

import org.deltava.security.SecurityContext;

/**
 * An access controller for Flight Academy Instructor busy time entries.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class BusyTimeAccessControl extends AccessControl {
	
	private final InstructionBusy _busy;
	
	private boolean _canCreate;
	private boolean _canProxyCreate;
	private boolean _canDelete;

	/**
	 * Initializes the Access Controller.
	 * @param ctx the security context
	 * @param ib the InstructionBusy bean
	 */
	public BusyTimeAccessControl(SecurityContext ctx, InstructionBusy ib) {
		super(ctx);
		_busy = ib;
	}

	/**
	 * Calculates access rights.
	 */
	@Override
	public void validate() {
		validateContext();
		
		// Check create access
		_canProxyCreate = _ctx.isUserInRole("AcademyAdmin") || _ctx.isUserInRole("HR");
		_canCreate = _ctx.isUserInRole("Instructor") || _canProxyCreate;
		if (!_ctx.isAuthenticated() || (_busy == null))
			return;

		// Check delete access
		_canDelete = (_busy.getID() == _ctx.getUser().getID()) || _canProxyCreate;
	}
	
	/**
	 * Returns whether a new Instructor busy time entry can be created.
	 * @return TRUE if a new entry can be created, otherwise FALSE
	 */
	public boolean getCanCreate() {
		return _canCreate;
	}
	
	/**
	 * Returns whether a new Instructor busy time entry can be created <i>for another Instructor</i>.
	 * @return TRUE if a new entry can be created for another instructor, otherwise FALSE
	 */
	public boolean getCanProxyCreate() {
		return _canProxyCreate;
	}
	
	/**
	 * Returns whether this Instructor busy time entry can be deleted.
	 * @return TRUE if the entry can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}