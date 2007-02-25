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

public final class CrossAppPilotAccessControl extends PilotAccessControl {

	private boolean _canViewEmail;

	/**
	 * Initializes the Access controller.
	 * @param ctx the command context
	 * @param p the Pilot profile
	 */
	public CrossAppPilotAccessControl(SecurityContext ctx, Pilot p) {
		super(ctx, p);
	}

	/**
	 * Calculates access rights.
	 */
	public void validate() {
		validateContext();
		if (_p == null)
			return;

		_canViewEmail = _ctx.isAuthenticated() ? _ctx.isUserInRole("HR") : (_p.getEmailAccess() == Person.SHOW_EMAIL);
	}

	/**
	 * Returns if we are currently accessing our own profile.
	 * @return FALSE
	 */
	public boolean getIsOurs() {
		return false;
	}

	/**
	 * Returns if the pilot profile can be edited.
	 * @return FALSE
	 */
	public boolean getCanEdit() {
		return false;
	}

	/**
	 * Returns if the Pilot can be pleaced On Leave.
	 * @return FALSE
	 */
	public boolean getCanTakeLeave() {
		return false;
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
	 * @return FALSE
	 */
	public boolean getCanViewExams() {
		return false;
	}

	/**
	 * Returns if the pilot's status can be edited.
	 * @return FALSE
	 */
	public boolean getCanChangeStatus() {
		return false;
	}
	
	/**
	 * Returns if the Pilot can be suspended/blocked.
	 * @return FALSE
	 */
	public boolean getCanSuspend() {
		return false;
	}
	
	/**
	 * Returns if a check ride can be assigned to this Pilot.
	 * @return FALSE
	 */
	public boolean getCanAssignRide() {
		return false;
	}

	/**
	 * Returns if the pilot can be promoted.
	 * @return FALSE;
	 */
	public boolean getCanPromote() {
		return false;
	}

	/**
	 * Returns if the pilot's access roles can be edited.
	 * @return FALSE
	 */
	public boolean getCanChangeRoles() {
		return false;
	}

	/**
	 * Returns if the pilot's staff profile can be edited.
	 * @return FALSE
	 */
	public boolean getCanChangeStaffProfile() {
		return false;
	}
	
	/**
	 * Returns if the pilot's IMAP mailbox profile can be edited.
	 * @return FALSE
	 */
	public boolean getCanChangeMailProfile() {
		return false;
	}

	/**
	 * Returns if the pilot's Water Cooler signature image can be edited.
	 * @return FALSE
	 */
	public boolean getCanChangeSignature() {
		return false;
	}
	
	/**
	 * Returns if the Pilot can be transferred to another airline.
	 * @return FALSE
	 */
	public boolean getCanTransfer() {
		return false;
	}
	
	/**
	 * Returns if the pilot can be activated.
	 * @return FALSE
	 */
	public boolean getCanActivate() {
	   return false;
	}
}