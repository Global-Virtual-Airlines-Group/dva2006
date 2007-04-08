// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import java.util.Date;

import org.deltava.security.SecurityContext;

import org.deltava.beans.testing.*;

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
    private boolean _canViewAnswers;
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
     * @throws AccessControlException if we cannot view the data
     */
    public void validate() throws AccessControlException {
       validateContext();
       
        // Check if we're authenticated
        if (!_ctx.isAuthenticated())
            throw new AccessControlException("Cannot view Examination");

        // Set access variables
        boolean isHR = _ctx.isUserInRole("HR");
        boolean isOurs = (_ctx.getUser().getID() == _t.getPilotID());
        boolean isExam = isHR || (_t.getAcademy() ? _ctx.isUserInRole("Examiner") : _ctx.isUserInRole("Examination"));
        
        // With checkrides, NEW == SUBMITTED
        boolean isCR = (_t instanceof CheckRide);
        boolean isSubmitted = (_t.getStatus() == Test.SUBMITTED);
        boolean isScored = (_t.getStatus() == Test.SCORED);
        if (!isCR) {
        	Examination ex = (Examination) _t;
        	isSubmitted = isSubmitted || ((_t.getStatus() == Test.NEW) && (ex.getExpiryDate().before(new Date())));
        }

        // Set access
        _canRead = isOurs || isExam || isHR || _ctx.isUserInRole("Instructor");
        _canSubmit = isOurs && !isCR && !isSubmitted && !isScored;
        _canEdit = isScored && isHR && !isOurs;
        _canDelete = _ctx.isUserInRole("Admin");
        _canScore = _canEdit || (isSubmitted && isExam);
        _canViewAnswers = isScored && (isHR || isExam || (_t.getAcademy() && _ctx.isUserInRole("Instructor")));
        
        // Throw an exception if we cannot view
        if (!_canRead)
            throw new AccessControlException("Cannot view Examination");
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
    
    /**
     * Returns if the correct answers to this Test's questions can be viewed.
     * @return TRUE if the correct answers can be viewed, otherwise FALSE
     */
    public boolean getCanViewAnswers() {
    	return _canViewAnswers;
    }
}