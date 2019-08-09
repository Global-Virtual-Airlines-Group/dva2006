// Copyright 2005, 2006, 2009, 2010, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.testing.EquipmentRideScript;

import org.deltava.security.SecurityContext;

/**
 * An access controller for Equipment Program Check Ride scripts.
 * @author Luke
 * @version 8.6
 * @since 1.0
 */

public class EquipmentRideScriptAccessControl extends AccessControl {

	private final EquipmentRideScript _sc;

	private boolean _canCreate;
	private boolean _canEdit;
	private boolean _canDelete;

	/**
	 * Initializes the access controller.
	 * @param ctx the security context
	 * @param sc the CheckRideScript bean to validate
	 */
	public EquipmentRideScriptAccessControl(SecurityContext ctx, EquipmentRideScript sc) {
		super(ctx);
		_sc = sc;
	}

	/**
	 * Calculates access rights.
	 * @throws AccessControlException if we cannot read the script
	 */
	@Override
	public void validate() throws AccessControlException {
		validateContext();

		// Do nothing if we are not in the exam role
		boolean isOps = _ctx.isUserInRole("Operations");
		boolean isHR = _ctx.isUserInRole("HR");
		if (!_ctx.isUserInRole("Examination") && !isOps & !isHR)
			throw new AccessControlException("Cannot view Check Ride script");

		// Check creation/deletion access
		_canCreate = true;
		if (_sc == null) {
			_canEdit = true;
			return;
		}

		_canDelete = isHR || isOps;
		
		// Allow edits if we are in the same eq program
		_canEdit = isOps || isHR || (_ctx.getUser().getEquipmentType().equals(_sc.getProgram()));
	}

	/**
	 * Returns whether a new Check Ride script can be created.
	 * @return TRUE if a script can be created, otherwise FALSE
	 */
	public boolean getCanCreate() {
		return _canCreate;
	}

	/**
	 * Returns whether the Check Ride script can be edited.
	 * @return TRUE if the script can be edited, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}

	/**
	 * Returns whether the Check Ride script can be deleted.
	 * @return TRUE if the script can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}