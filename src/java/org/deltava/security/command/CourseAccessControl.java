// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import java.util.Iterator;

import org.deltava.beans.academy.*;
import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Flight Academy Course profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CourseAccessControl extends AccessControl {
	
	private Course _c;
	private boolean _canComment;
	private boolean _canCancel;
	private boolean _canRestart;
	private boolean _canStart;
	private boolean _canApprove;
	private boolean _canAssign;
	private boolean _canUpdateProgress;
	private boolean _canSchedule;
	private boolean _canDelete;

	/**
	 * Initializes the Access Controller.
	 * @param ctx the security context
	 * @param c the Course bean 
	 */
	public CourseAccessControl(SecurityContext ctx, Course c) {
		super(ctx);
		_c = c;
	}

	/**
	 * Calculates access rights.
	 * @throws AccessControlException if the user cannot view the data 
	 */
	public void validate() throws AccessControlException {
		validateContext();
		if (!_ctx.isAuthenticated())
			throw new AccessControlException("Not Authorized");

		// Define conditions
		boolean isHR = _ctx.isUserInRole("HR");
		boolean isExaminer = _ctx.isUserInRole("AcademyAdmin") || _ctx.isUserInRole("Examiner");
		boolean isINS = _ctx.isUserInRole("Instructor") || isExaminer;
		boolean isMine = (_ctx.getUser().getID() == _c.getPilotID());
		boolean isStarted = (_c.getStatus() == Course.STARTED);
		boolean isPending = (_c.getStatus() == Course.PENDING);
		if (!isMine && !isINS && !isHR)
			throw new AccessControlException("Not Authorized");
		
		// Assign access rights
		_canComment = isINS || isHR || (isMine && isStarted);
		_canStart = (isINS || isHR) && (isPending || (_c.getStatus() == Course.ABANDONED));
		_canCancel = (isHR || isMine || _ctx.isUserInRole("AcademyAdmin")) && isStarted;
		_canRestart = (_c.getStatus() == Course.ABANDONED) && isMine;
		_canUpdateProgress = (isHR || isINS) && isStarted && !isMine;
		_canSchedule = isStarted && (isHR || isINS);
		_canAssign = (isStarted || isPending) && (isHR || isExaminer);
		_canDelete = _ctx.isUserInRole("Admin") || _canStart;
		
		// Check if we've met all of the requirements
		_canApprove = (isHR || isExaminer) && isStarted && !isMine;
		for (Iterator<CourseProgress> i = _c.getProgress().iterator(); _canApprove && i.hasNext(); ) {
			CourseProgress cp = i.next();
			_canApprove &= cp.getComplete();
		}
	}

	/**
	 * Returns if the user can add a comment to the Course.
	 * @return TRUE if a comment can be added, otherwise FALSE
	 */
	public boolean getCanComment() {
		return _canComment;
	}
	
	/**
	 * Returns if the user can update Course Progress.
	 * @return TRUE if progress can be edited, otherwise FALSE
	 */
	public boolean getCanUpdateProgress() {
		return _canUpdateProgress;
	}
	
	/**
	 * Returns if the user can start the Course. 
	 * @return TRUE if the course can be started, otherwise FALSE
	 */
	public boolean getCanStart() {
		return _canStart;
	}
	
	/**
	 * Returns if the user can cancel the Course.
	 * @return TRUE if the Course can be canceled, otherwise FALSE
	 */
	public boolean getCanCancel() {
		return _canCancel;
	}
	
	/**
	 * Returns if the user can restart the Course.
	 * @return TRUE if the Course can be restarted, otherwise FALSE
	 */
	public boolean getCanRestart() {
		return _canRestart;
	}
	
	/**
	 * Returns if the user can schedule a training session.
	 * @return TRUE if a Session can be scheduled, otherwise FALSE
	 */
	public boolean getCanSchedule() {
		return _canSchedule;
	}
	
	/**
	 * Returns if the user can assign an Instructor to this Course.
	 * @return TRUE if an Instructor can be assigned, otherwise FALSE
	 */
	public boolean getCanAssign() {
		return _canAssign;
	}
	
	/**
	 * Returns if the user can approve completion of the Course.
	 * @return TRUE if the Course can be marked complete, otherwise FALSE
	 */
	public boolean getCanApprove() {
		return _canApprove;
	}
	
	/**
	 * Returns if the user can delete the Course.
	 * @return TRUE if the Course can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}