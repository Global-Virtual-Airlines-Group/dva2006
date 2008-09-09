// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;

/**
 * An Access controller for dispatch data.
 * @author Luke
 * @version 2.2
 * @since 2.1
 */

public class DispatchRouteAccessControl extends AccessControl {
	
	private boolean _canCreate;
	private boolean _canView;
	private boolean _canDelete;

	/**
	 * Initializes the Access Controller.
	 * @param ctx the Security context
	 */
	public DispatchRouteAccessControl(SecurityContext ctx) {
		super(ctx);
	}

	/**
	 * Calculates access rights.
	 */
	public void validate() {
		validateContext();

		// Check access
		_canDelete = _ctx.isUserInRole("HR");
		_canCreate = _ctx.isUserInRole("Dispatch");
		_canView = _canCreate || _canDelete;
	}
	
	/**
	 * Returns whether new Dispatch routes can be created.
	 * @return TRUE if new routes can be created, otherwise FALSE
	 */
	public boolean getCanCreate() {
		return _canCreate;
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