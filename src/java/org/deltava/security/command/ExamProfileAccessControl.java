// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Examination Profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ExamProfileAccessControl extends AccessControl {
    
    private boolean _canRead;
    private boolean _canEdit;

    /**
     * Initialize the Access Controller.
     * @param ctx the Command Context
     */
    public ExamProfileAccessControl(SecurityContext ctx) {
        super(ctx);
    }

    /**
     * Calculates access rights.
     * @throws AccessControlException if the User cannot read the profile.
     */
    public void validate() throws AccessControlException {
       validateContext();
        
        // Check if we are a member of the HR role
        _canEdit = _ctx.isUserInRole("HR");
        _canRead = _canEdit || _ctx.isUserInRole("Examination");
        
        // If we cannot read the profile, shut us down
        if (!_canRead)
            throw new AccessControlException("Cannot view/edit Examination Profile");
    }
    
	/**
     * Returns if the profile can be viewed.
     * @return TRUE if it can be viewed, otherwise FALSE
     */
    public boolean getCanRead() {
        return _canRead;
    }

	/**
     * Returns if the profile can be edited.
     * @return TRUE if it can be edited, otherwise FALSE
     */
    public boolean getCanEdit() {
        return _canEdit;
    }
}