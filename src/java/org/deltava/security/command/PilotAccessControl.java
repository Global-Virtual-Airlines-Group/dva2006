// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;

import org.deltava.beans.Person;
import org.deltava.beans.Pilot;

/**
 * An access controller for Pilot profile operations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PilotAccessControl extends AccessControl {

	protected Pilot _p;

	private boolean _isOurs;
	private boolean _canViewEmail;
	private boolean _canTakeLeave;
	private boolean _canEdit;
	private boolean _canChangeStatus;
	private boolean _canAssignRide;
	private boolean _canPromote;
	private boolean _canChangeRoles;
	private boolean _canChangeSignature;
	private boolean _canChangeStaffProfile;
	private boolean _canChangeMailProfile;
	private boolean _canViewExams;
	private boolean _canTransfer;
	private boolean _canSuspend;
	private boolean _canActivate;

	/**
	 * Initializes the Access controller.
	 * @param ctx the command context
	 * @param p the Pilot profile
	 */
	public PilotAccessControl(SecurityContext ctx, Pilot p) {
		super(ctx);
		_p = p;
	}

	/**
	 * Calculates access rights.
	 */
	public void validate() {
		validateContext();
		if (_p == null)
			return;

		// Get the currently logged in user. If not logged in, just check e-mail access
		if (!_ctx.isAuthenticated()) {
			_canViewEmail = (_p.getEmailAccess() == Person.SHOW_EMAIL);
			return;
		}

		// Sets basic role variables
		_isOurs = (_ctx.getUser().getID() == _p.getID());
		boolean isPIREP = _ctx.isUserInRole("PIREP");
		boolean isHR = _ctx.isUserInRole("HR");
		int status = _p.getStatus();

		// Set parameters
		_canEdit = (_isOurs || isPIREP || isHR);
		_canChangeSignature = _canEdit || _ctx.isUserInRole("Signature");
		_canViewEmail = (_p.getEmailAccess() == Person.HIDE_EMAIL) ? (_canEdit) : true;
		_canPromote = (isPIREP || isHR);
		_canViewExams = _isOurs || _canPromote || _ctx.isUserInRole("Instructor");
		_canAssignRide = (isHR || _ctx.isUserInRole("Examination")) && (_p.getStatus() == Pilot.ACTIVE);
		_canChangeStatus = isHR;
		_canTakeLeave = (status == Pilot.ACTIVE) && (_isOurs || _canChangeStatus);
		_canChangeRoles = _ctx.isUserInRole("Admin");
		_canTransfer = _canChangeStatus && (status != Pilot.TRANSFERRED);
		_canSuspend = _canChangeStatus && ((status == Pilot.ACTIVE) || (status == Pilot.ON_LEAVE)); 
		_canActivate = _canChangeStatus && ((status == Pilot.INACTIVE) || (status == Pilot.RETIRED) || 
				(status == Pilot.SUSPENDED));

		// Check if there is a staff profile in the request
		Object sProfile = _ctx.getRequest().getAttribute("staff");
		if (sProfile != null)
			_canChangeStaffProfile = (_isOurs || isHR);
		
		// Check if there's an IMAP mailbox profile in the reuqest
		Object mProfile = _ctx.getRequest().getAttribute("emailCfg");
		_canChangeMailProfile = ((mProfile != null) && isHR) || ((mProfile == null) && _ctx.isUserInRole("Admin")); 
	}

	/**
	 * Returns if we are currently accessing our own profile.
	 * @return TRUE if the profile is that of the currently logged in user, otherwise FALSE
	 */
	public boolean getIsOurs() {
		return _isOurs;
	}

	/**
	 * Returns if the pilot profile can be edited.
	 * @return TRUE if the profile can be edited, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}

	/**
	 * Returns if the Pilot can be pleaced On Leave.
	 * @return TRUE if the Pilot can be placed on Leave, otherwise FALSE
	 */
	public boolean getCanTakeLeave() {
		return _canTakeLeave;
	}

	/**
	 * Returns if the e-mail address can be viewed.
	 * @return TRUE if the address can be viewed, otherwise FALSE
	 */
	public boolean getCanViewEmail() {
		return _canViewEmail;
	}
	
	/**
	 * Returns if the Pilot's examination history can be viewed.
	 * @return TRUE if the exams can be viewed, otherwise FALSE
	 */
	public boolean getCanViewExams() {
		return _canViewExams;
	}

	/**
	 * Returns if the pilot's status can be edited.
	 * @return TRUE if the status can be edited, otherwise FALSE
	 */
	public boolean getCanChangeStatus() {
		return _canChangeStatus;
	}
	
	/**
	 * Returns if the Pilot can be suspended/blocked.
	 * @return TRUE if the Pilot can be blocked, otherwise FALSE
	 */
	public boolean getCanSuspend() {
		return _canSuspend;
	}
	
	/**
	 * Returns if a check ride can be assigned to this Pilot.
	 * @return TRUE if a check ride can be assigned, otherwise FALSE
	 */
	public boolean getCanAssignRide() {
		return _canAssignRide;
	}

	/**
	 * Returns if the pilot can be promoted.
	 * @return TRUE if the rank/equipment type can be edited, otherwise FALSE
	 */
	public boolean getCanPromote() {
		return _canPromote;
	}

	/**
	 * Returns if the pilot's access roles can be edited.
	 * @return TRUE if the access roles can be edited, otherwise FALSE
	 */
	public boolean getCanChangeRoles() {
		return _canChangeRoles;
	}

	/**
	 * Returns if the pilot's staff profile can be edited.
	 * @return TRUE if the staff profile can be edited, otherwise FALSE
	 */
	public boolean getCanChangeStaffProfile() {
		return _canChangeStaffProfile;
	}
	
	/**
	 * Returns if the pilot's IMAP mailbox profile can be edited.
	 * @return TRUE if the mailbox profile can be edited, otherwise FALSE
	 */
	public boolean getCanChangeMailProfile() {
		return _canChangeMailProfile;
	}

	/**
	 * Returns if the pilot's Water Cooler signature image can be edited.
	 * @return TRUE if the signature image can be edited, otherwise FALSE
	 */
	public boolean getCanChangeSignature() {
		return _canChangeSignature;
	}
	
	/**
	 * Returns if the Pilot can be transferred to another airline.
	 * @return TRUE if the Pilot can be transferred, otherwise FALSE
	 */
	public boolean getCanTransfer() {
		return _canTransfer;
	}
	
	/**
	 * Returns if the pilot can be activated. This is a convenience method, since it just checks if the Pilot
	 * is Inactive or Retired, and the canChangeStatus property is TRUE.
	 * @return TRUE if the Pilot can be activated, otherwise FALSE
	 * @see PilotAccessControl#getCanChangeStatus()
	 */
	public boolean getCanActivate() {
	   return _canActivate;
	}
}