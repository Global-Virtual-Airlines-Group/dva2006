// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.acars.ACARSError;

import org.deltava.security.SecurityContext;

/**
 * An access controller for ACARS client error reports.
 * @author Luke
 * @version 9.1
 * @since 9.1
 */

public class ErrorLogAccessControl extends AccessControl {
	
	private final ACARSError _err;
	
	private boolean _canSubmit;
	private boolean _canRead;
	private boolean _canDelete;

	/**
	 * Initializes the Access Controller.
	 * @param ctx the SecurityContext
	 * @param err the ACARSError
	 */
	public ErrorLogAccessControl(SecurityContext ctx, ACARSError err) {
		super(ctx);
		_err = err;
	}

	@Override
	public void validate() {
		validateContext();
		
		_canSubmit = _ctx.isAuthenticated();
		_canDelete = _ctx.isUserInRole("Developer");
		_canRead = _ctx.isUserInRole("Developer") || (_ctx.isAuthenticated() && (_err != null) && (_err.getAuthorID() == _ctx.getUser().getID()));
	}

	/**
	 * Returns whether the user can submit a new Error Report.
	 * @return TRUE if a report can be submitted, otherwise FALSE
	 */
	public boolean getCanSubmit() {
		return _canSubmit;
	}

	/**
	 * Returns whether the user can read ths new Error Report.
	 * @return TRUE if the report can be read, otherwise FALSE
	 */
	public boolean getCanRead() {
		return _canRead;
	}
	
	/**
	 * Returns whether the user can delete an Error Report.
	 * @return TRUE if a report can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}