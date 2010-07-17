// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.stats.Accomplishment;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Accomplishment profiles. 
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

public class AccomplishmentAccessControl extends AccessControl {
	
	private Accomplishment _a;
	
	private boolean _canEdit;
	private boolean _canCreate;
	private boolean _canDelete;

	/**
	 * Initializes the Controller.
	 * @param ctx the Security Context
	 * @param a the Accomplishment
	 */
	public AccomplishmentAccessControl(SecurityContext ctx, Accomplishment a) {
		super(ctx);
		_a = a;
	}

	/**
	 * Calculates access rights.
	 */
	@Override
	public void validate() {
		_canCreate = _ctx.isUserInRole("HR") || _ctx.isUserInRole("Operations");
		_canEdit = (_a != null) && (_ctx.isUserInRole("HR") || _ctx.isUserInRole("Operations"));
		_canDelete = (_canEdit && (_a.getPilots() == 0)) || _ctx.isUserInRole("Admin");
	}

	/**
	 * Returns whether the user can create a new Accomplishment profile.
	 * @return TRUE if a new profile can be created, otherwise FALSE
	 */
	public boolean getCanCreate() {
		return _canCreate;
	}

	/**
	 * Returns whether the user can edit thtis Accomplishment profile.
	 * @return TRUE if the profile can be edited, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}

	/**
	 * Returns whether the user can delete thtis Accomplishment profile.
	 * @return TRUE if the profile can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}