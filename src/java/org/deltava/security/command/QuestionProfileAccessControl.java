// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Examination Question Profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class QuestionProfileAccessControl extends AccessControl {

    private boolean _canRead;
    private boolean _canEdit;
    
    /**
     * Initializes the Access Controller.
     * @param ctx the command context
     */
    public QuestionProfileAccessControl(SecurityContext ctx) {
        super(ctx);
    }

    /**
     * Calculates access rights.
     * @throws AccessControlException if the user cannot read the profile
     */
    public void validate() throws AccessControlException {
       validateContext();
        
        // Calculate access variables
        boolean isHR = _ctx.isUserInRole("HR");
        boolean isExam = _ctx.isUserInRole("Examination"); 
        
        _canRead = isHR || isExam;
        _canEdit = isHR || isExam;
        if (_canRead)
        	throw new AccessControlException("Cannot view Question Profile");
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