// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;

/**
 * An Access controller for dispatch data.
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public class DispatchAccessControl extends AccessControl {
	
	private boolean _canView;
	private boolean _canDelete;

	/**
	 * Initializes the Access Controller.
	 * @param ctx the Security context
	 */
	public DispatchAccessControl(SecurityContext ctx) {
		super(ctx);
	}

	/**
	 * Calculates access rights.
	 */
	public void validate() {
		validateContext();

		// Check access
		_canDelete = _ctx.isUserInRole("HR");
		_canView = _ctx.isUserInRole("Dispatch") || _canDelete;
	}
	
	/**
	 * Returns whether the dispatch data can be viewed. 
	 * @return TRUE if it can be viewed, otherwise FALSE
	 */
	public boolean getCanView() {
		return _canView;
	}
	
	/**
	 * Returns whether the dispatch data can be deleted. 
	 * @return TRUE if it can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}