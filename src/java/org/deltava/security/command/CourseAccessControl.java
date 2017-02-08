// Copyright 2006, 2007, 2008, 2010, 2012, 2014, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.academy.*;
import org.deltava.beans.testing.*;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Flight Academy Course profiles.
 * @author Luke
 * @version 7.2
 * @since 1.0
 */

public class CourseAccessControl extends AccessControl {
	
	private final Course _c;
	private Certification _crt;
	
	private boolean _canComment;
	private boolean _canCancel;
	private boolean _canRestart;
	private boolean _canStart;
	private boolean _canApprove;
	private boolean _canAssign;
	private boolean _canAssignCheckRide;
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
	 * Sets the Certification, used for validating Online courses.
	 * @param crt the Certification bean
	 */
	public void setCertification(Certification crt) {
		_crt = crt;
	}

	/**
	 * Calculates access rights.
	 * @throws AccessControlException if the user cannot view the data 
	 */
	@Override
	public void validate() throws AccessControlException {
		validateContext();
		if (!_ctx.isAuthenticated())
			throw new AccessControlException("Not Authorized");

		// Define conditions
		boolean isHR = _ctx.isUserInRole("HR");
		boolean isINS = _ctx.isUserInRole("Instructor") || _ctx.isUserInRole("AcademyAdmin");
		boolean isMine = (_ctx.getUser().getID() == _c.getPilotID());
		boolean isStarted = (_c.getStatus() == Status.STARTED);
		boolean isPending = (_c.getStatus() == Status.PENDING);
		if (!isMine && !isINS && !isHR && !_ctx.isUserInRole("AcademyAudit"))
			throw new AccessControlException("Not Authorized");
		
		// Assign access rights
		_canComment = isINS || isHR || (isMine && isStarted);
		_canStart = (isINS || isHR) && (isPending || (_c.getStatus() == Status.ABANDONED));
		_canCancel = (isHR || isMine || _ctx.isUserInRole("AcademyAdmin")) && (isStarted || isPending);
		_canRestart = (_c.getStatus() == Status.ABANDONED) && isMine;
		_canUpdateProgress = (isHR || isINS) && isStarted && !isMine;
		_canSchedule = isStarted && (isHR || isINS);
		_canAssign = (isStarted || isPending) && (isHR || _ctx.isUserInRole("AcademyAdmin"));
		_canDelete = _ctx.isUserInRole("Admin") || _canStart;
		
		// Check if we've met all of the requirements
		boolean isComplete = true;
		for (CourseProgress cp : _c.getProgress())
			isComplete &= cp.getComplete();
		
		// Check if we need a Check Ride
		int completedRides = 0; boolean hasPendingRide = false;
		for (CheckRide cr : _c.getCheckRides()) {
			hasPendingRide |= (cr.getStatus() != TestStatus.SCORED);
			if (cr.getPassFail())
				completedRides++;
		}
		
		boolean crComplete = (completedRides == _c.getRideCount());
		_canAssignCheckRide = isComplete && isStarted && (_c.getRideCount() > 0) && !hasPendingRide && !crComplete;
		_canApprove = (_crt != null) && crComplete && isComplete && (isHR || isINS) && isStarted && !isMine && ((_crt.getNetwork() == null) || _ctx.getUser().hasNetworkID(_crt.getNetwork()));
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
	public boolean getCanAssignInstructor() {
		return _canAssign;
	}
	
	/**
	 * Returns if the user can assign a Check Ride for this Course.
	 * @return TRUE if a Check Ride can be assigned, otherwise FALSE
	 */
	public boolean getCanAssignCheckRide() {
		return _canAssignCheckRide;
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