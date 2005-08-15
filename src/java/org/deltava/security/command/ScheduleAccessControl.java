// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.commands.CommandSecurityException;
import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Route data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ScheduleAccessControl extends AccessControl {
	
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

    /**
     * Calculates access rights.
     * @throws CommandSecurityException never
     */
	public void validate() throws CommandSecurityException {
		validateContext();

		// Set role fields
		_canDelete = _ctx.isUserInRole("Schedule");
		_canImport = _canDelete;
		_canExport = _canImport;
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