// Copyright 2007, 2008, 2009, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.acars.DispatchRoute;

import org.deltava.security.SecurityContext;

/**
 * An Access controller for dispatch data.
 * @author Luke
 * @version 7.0
 * @since 2.1
 */

public class DispatchRouteAccessControl extends AccessControl {
	
	private final DispatchRoute _rt;
	
	private boolean _canCreate;
	private boolean _canView;
	private boolean _canDisable;
	private boolean _canDelete;

	/**
	 * Initializes the Access Controller.
	 * @param ctx the Security context
	 * @param rt the Dispatch Route
	 */
	public DispatchRouteAccessControl(SecurityContext ctx, DispatchRoute rt) {
		super(ctx);
		_rt = rt;
	}

	/**
	 * Calculates access rights.
	 */
	@Override
	public void validate() {
		validateContext();
		
		// Check access
		_canCreate = _ctx.isUserInRole("Route");
		_canDisable = _canCreate;
		_canDelete = _ctx.isUserInRole("Admin") && (_rt != null) && (_rt.getUseCount() == 0);
		
		// Check view access
		boolean isActive = (_rt != null) && _rt.getActive();
		_canView = _canCreate || _ctx.isUserInRole("Route") || (isActive && _ctx.isUserInRole("Pilot"));
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
	 * Returns whether the Dispatch route can be disabled.
	 * @return TRUE if it can be disabled, otherwise FALSE
	 */
	public boolean getCanDisable() {
		return _canDisable;
	}
	
	/**
	 * Returns whether the dispatch data can be deleted. 
	 * @return TRUE if it can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}