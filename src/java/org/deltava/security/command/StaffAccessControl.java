// Copyright 2005, 2006, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;

import org.deltava.beans.Staff;

/**
 * An access controller for Staff Profile operations.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public final class StaffAccessControl extends AccessControl {
	
	private final Staff _sp;
	
	private boolean _canEdit;
	private boolean _canDelete;
	private boolean _canCreate;

	/**
	 * Initializes the controller.
	 * @param ctx the Command Context
	 * @param s the Staff Profile to be operated on
	 */
	public StaffAccessControl(SecurityContext ctx, Staff s) {
		super(ctx);
		_sp = s;
	}

    /**
     * Calculates access rights.
     */
	@Override
	public void validate() {
		validateContext();
		
        // Set role variables
		boolean hasProfile = (_sp != null); 
        boolean isOurs = (_ctx.isAuthenticated() && hasProfile && (_sp.getID() == _ctx.getUser().getID()));
        boolean isHR = _ctx.isUserInRole("HR");
        
        // Set operation permission
        _canEdit = hasProfile && (isOurs || isHR);
        _canDelete = hasProfile && isHR;
        _canCreate = isHR;
	}
	
    /**
     * Returns if the profile can be edited.
     * @return TRUE if it can be edited, otherwise FALSE
     */
	public boolean getCanEdit() {
		return _canEdit;
	}
	
	/**
     * Returns if the profile can be deleted.
     * @return TRUE if it can be deleted, otherwise FALSE
     */
	public boolean getCanDelete() {
		return _canDelete;
	}
	
	/**
	 * Returns if a new profile can be created.
	 * @return TRUE if a profile can be created, otherwise FAL
	 */
	public boolean getCanCreate() {
	    return _canCreate;
	}
}