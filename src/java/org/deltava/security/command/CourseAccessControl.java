// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.academy.Course;

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
	private boolean _canApprove;
	private boolean _canUpdateProgress;
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
		boolean isINS = _ctx.isUserInRole("Instructor");
		boolean isMine = (_ctx.getUser().getID() == _c.getPilotID());
		boolean isStarted = (_c.getStatus() == Course.STARTED);
		if (!isMine && !isINS && !isHR)
			throw new AccessControlException("Not Authorized");
		
		// Assign access rights
		_canComment = isINS || isHR || (isMine && isStarted);
		_canCancel = (isHR || isMine) && isStarted;
		_canRestart = (_c.getStatus() == Course.ABANDONED) && (isMine || isINS || isHR);
		_canApprove = isHR && isStarted;
		_canUpdateProgress = (isHR || isINS) && isStarted;
		_canDelete = _ctx.isUserInRole("Admin");
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