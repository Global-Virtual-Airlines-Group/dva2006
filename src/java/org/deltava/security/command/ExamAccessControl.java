// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;
import org.deltava.commands.CommandSecurityException;

import org.deltava.beans.testing.Test;

/**
 * An Access Controller for Pilot Examinations and Check Ride records.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ExamAccessControl extends AccessControl {

    private Test _t;
    
    private boolean _canRead;
    private boolean _canSubmit;
    private boolean _canScore;
    private boolean _canEdit;
    private boolean _canDelete;
    
    /**
     * Initialize the Access controller.
     * @param ctx the command context
     * @param t the Examination/CheckRide to validate against
     */
    public ExamAccessControl(SecurityContext ctx, Test t) {
        super(ctx);
        _t = t;
    }

    /**
     * Calculates access rights.
     * @throws CommandSecurityException if we cannot view the data
     */
    public void validate() throws CommandSecurityException {
       validateContext();
       
        // Check if we're authenticated
        if (!_ctx.isAuthenticated())
            throw new CommandSecurityException("Cannot view Examination");

        // Set access variables
        boolean isOurs = (_ctx.getUser().getID() == _t.getPilotID());
        boolean isExam = _ctx.isUserInRole("Examination");
        boolean isHR = _ctx.isUserInRole("HR");
        
        // Set access
        _canRead = isOurs || isExam || isHR;
        _canSubmit = isOurs && (_t.getStatus() == Test.NEW);
        _canEdit = (_t.getStatus() == Test.SCORED) && isHR && !isOurs;
        _canScore = _canEdit || ((_t.getStatus() == Test.SUBMITTED) && (isExam || isHR) && !isOurs);
        _canDelete = _ctx.isUserInRole("Admin");
        
        // Throw an exception if we cannot view
        if (!_canRead)
            throw new CommandSecurityException("Cannot view Examination");
    }
    
 	/**
     * Returns if the Test can be read.
     * @return TRUE if the Test can be read, otherwise FALSE
     */
    public boolean getCanRead() {
        return _canRead;
    }
   
    /**
     * Returns if the Test can be submitted.
     * @return TRUE if the Test can be submitted, otherwise FALSE
     */
    public boolean getCanSubmit() {
        return _canSubmit;
    }
    
    /**
     * Returns if the Test can be scored.
     * @return TRUE if the Test can be scored, otherwise FALSE
     */
    public boolean getCanScore() {
        return _canScore;
    }
    
    /**
     * Returns if the Test can be edited.
     * @return TRUE if the Test can be edited, otherwise FALSE
     */
    public boolean getCanEdit() {
        return _canEdit;
    }
    
    /**
     * Returns if the Test can be deleted.
     * @return TRUE if the Test can be deleted, otherwise FALSE
     */
    public boolean getCanDelete() {
        return _canDelete;
    }
}