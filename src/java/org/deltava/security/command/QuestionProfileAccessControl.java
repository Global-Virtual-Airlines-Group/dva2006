// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;
import org.deltava.commands.CommandSecurityException;

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
     * @throws CommandSecurityException never
     */
    public void validate() throws CommandSecurityException {
       validateContext();
        
        // Calculate access variables
        boolean isHR = _ctx.isUserInRole("HR");
        boolean isExam = _ctx.isUserInRole("Examination"); 
        
        _canRead = isHR || isExam;
        _canEdit = isHR || isExam;
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