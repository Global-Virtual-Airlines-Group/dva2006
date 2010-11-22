// Copyright 2005, 2006, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.academy.AcademyRideScript;

import org.deltava.security.SecurityContext;

/**
 * An access controller for Fight Academy Check Ride scripts.
 * @author Luke
 * @version 3.4
 * @since 3.4
 */

public class AcademyRideScriptAccessControl extends AccessControl {
	
	private AcademyRideScript _sc;

	private boolean _canCreate;
	private boolean _canEdit;
	private boolean _canDelete;

	/**
	 * Initializes the access controller.
	 * @param ctx the security context
	 * @param sc the AcademyRideScript bean to validate
	 */
	public AcademyRideScriptAccessControl(SecurityContext ctx, AcademyRideScript sc) {
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
		boolean isINS = _ctx.isUserInRole("Instructor") || _ctx.isUserInRole("Examiner");
		boolean isAdmin = _ctx.isUserInRole("HR") || _ctx.isUserInRole("AcademyAdmin");
		if (!isINS & !isAdmin)
			throw new AccessControlException("Cannot view Check Ride script");

		// Check creation/deletion access
		_canCreate = true;
		_canEdit = true;
		_canDelete = (_sc != null) && isAdmin;
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