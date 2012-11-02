// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.schedule.Aircraft;

import org.deltava.security.SecurityContext;
import org.deltava.util.system.SystemData;

/**
 * A Security Controller for Aircraft profiles.
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class AircraftAccessControl extends AccessControl {

	private final Aircraft _a;
	
	private boolean _canCreate;
	private boolean _canRead;
	private boolean _canEdit;
	private boolean _canDelete;
	
	/**
	 * Initializes the controller.
	 * @param ctx the SecurityContext
	 * @param a the Aircraft profile
	 */
	public AircraftAccessControl(SecurityContext ctx, Aircraft a) {
		super(ctx);
		_a = a;
	}

	/**
	 * Calculates access rights.
	 */
	@Override
	public void validate() {
		
		// Calculate create role
		boolean isOps = _ctx.isUserInRole("Operations");
		_canCreate = isOps;
		if (_a == null)
			return;

		boolean isSched = _ctx.isUserInRole("Schedule");
		_canRead = (_ctx.isUserInRole("Pilot") && _a.isUsed(SystemData.get("airline.code"))) || isOps || isSched; 
		_canEdit = isOps;
		_canDelete = _ctx.isUserInRole("Admin");
	}

	/**
	 * Returns whether the user can create a new Aircraft profile.
	 * @return TRUE if a new profile can be created, otherwise FALSE
	 */
	public boolean getCanCreate() {
		return _canCreate;
	}
	
	/**
	 * Returns whether the user can read this Aircraft profile.
	 * @return TRUE if this profile can be read, otherwise FALSE
	 */
	public boolean getCanRead() {
		return _canRead;
	}
	
	/**
	 * Returns whether the user can edit this Aircraft profile.
	 * @return TRUE if this profile can be edited, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}
	
	/**
	 * Returns whether the user can delete this Aircraft profile.
	 * @return TRUE if this profile can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}