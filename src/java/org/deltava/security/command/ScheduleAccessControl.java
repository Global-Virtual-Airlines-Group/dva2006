// Copyright 2005, 2006, 2009, 2016, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Flight Schdeule data.
 * @author Luke
 * @version 9.1
 * @since 1.0
 */

public class ScheduleAccessControl extends AccessControl {
	
	private boolean _canEdit;
	private boolean _canDelete;
	private boolean _canImport;
	private boolean _canExport;
	
	/**
	 * Initializes the Access Controller.
	 * @param ctx the Command context
	 */
	public ScheduleAccessControl(SecurityContext ctx) {
		super(ctx);
	}

	@Override
	public void validate() {
		validateContext();

		// Set role fields
		boolean hasRole = _ctx.isUserInRole("Schedule") || _ctx.isUserInRole("Operations");
		_canEdit = hasRole;
		_canDelete = hasRole;
		_canImport = hasRole;
		_canExport = hasRole;
	}
	
	/**
	 * Returns if route data can be modified.
	 * @return TRUE if the rotue data can be modified, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}

	/**
	 * Returns if route data can be deleted.
	 * @return TRUE if the route data can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
	
	/**
	 * Returns if Flight Schedule data can be imported.
	 * @return TRUE if data can be imported, otherwise FALSE
	 */
	public boolean getCanImport() {
		return _canImport;
	}
	
	/**
	 * Returns if Flight Schedule data can be exported.
	 * @return TRUE if data can be exported, otherwise FALSE
	 */
	public boolean getCanExport() {
	   return _canExport;
	}
}