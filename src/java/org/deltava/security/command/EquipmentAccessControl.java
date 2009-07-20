// Copyright 2005, 2006, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Equipment Type profile operations.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public final class EquipmentAccessControl extends AccessControl {

	private boolean _canEdit;
	private boolean _canRename;
	private boolean _canDelete;
	
	/**
	 * Initializes the Access Controller.
	 * @param ctx the Command context
	 */
	public EquipmentAccessControl(SecurityContext ctx) {
		super(ctx);
	}

    /**
     * Calculates access rights.
     */
	public void validate() {
		validateContext();

		// Update access rights
		_canEdit = _ctx.isUserInRole("HR") || _ctx.isUserInRole("Operations");
		_canRename = _ctx.isUserInRole("Admin");
		_canDelete = _canRename;
	}
	
	/**
     * Returns if the profile can be edited.
     * @return TRUE if it can be edited, otherwise FALSE
     */
	public boolean getCanEdit() {
		return _canEdit;
	}
	
	/**
     * Returns if the equipment type can be renamed.
     * @return TRUE if it can be renamed, otherwise FALSE
     */
	public boolean getCanRename() {
		return _canRename;
	}
	
	/**
     * Returns if the profile can be deleted.
     * @return TRUE if it can be deleted, otherwise FALSE
     */
	public boolean getCanDelete() {
		return _canDelete;
	}
}