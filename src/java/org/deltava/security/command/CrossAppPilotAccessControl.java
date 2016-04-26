// Copyright 2005, 2006, 2007, 2008, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;

import org.deltava.beans.Person;
import org.deltava.beans.Pilot;

/**
 * An access controller for Pilot profile operations performed by users in another
 * airline. This is designed to allow HR staffs in one airline to review the status of
 * a Pilot before a Transfer.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public final class CrossAppPilotAccessControl extends PilotAccessControl {

	private boolean _canViewExams;
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
	@Override
	public void validate() {
		validateContext();
		if (_p == null)
			return;

		_canViewExams = _ctx.isUserInRole("HR");
		_canViewEmail = _ctx.isAuthenticated() ? _ctx.isUserInRole("HR") : (_p.getEmailAccess() == Person.SHOW_EMAIL);
	}

	/**
	 * Returns if we are currently accessing our own profile.
	 * @return FALSE
	 */
	@Override
	public boolean getIsOurs() {
		return false;
	}

	/**
	 * Returns if the pilot profile can be edited.
	 * @return FALSE
	 */
	@Override
	public boolean getCanEdit() {
		return false;
	}

	/**
	 * Returns if the Pilot can be pleaced On Leave.
	 * @return FALSE
	 */
	@Override
	public boolean getCanTakeLeave() {
		return false;
	}

	/**
	 * Returns if the e-mail address can be viewed.
	 * @return TRUE if the address can be viewed, otherwise FALSE
	 */
	@Override
	public boolean getCanViewEmail() {
		return _canViewEmail;
	}
	
	/**
	 * Returns if the Pilot's examination history can be viewed.
	 * @return FALSE
	 */
	@Override
	public boolean getCanViewExams() {
		return _canViewExams;
	}

	/**
	 * Returns if the pilot's status can be edited.
	 * @return FALSE
	 */
	@Override
	public boolean getCanChangeStatus() {
		return false;
	}
	
	/**
	 * Returns if a check ride can be assigned to this Pilot.
	 * @return FALSE
	 */
	@Override
	public boolean getCanAssignRide() {
		return false;
	}

	/**
	 * Returns if the pilot can be promoted.
	 * @return FALSE;
	 */
	@Override
	public boolean getCanPromote() {
		return false;
	}

	/**
	 * Returns if the pilot's access roles can be edited.
	 * @return FALSE
	 */
	@Override
	public boolean getCanChangeRoles() {
		return false;
	}

	/**
	 * Returns if the pilot's staff profile can be edited.
	 * @return FALSE
	 */
	@Override
	public boolean getCanChangeStaffProfile() {
		return false;
	}
	
	/**
	 * Returns if the pilot's IMAP mailbox profile can be edited.
	 * @return FALSE
	 */
	@Override
	public boolean getCanChangeMailProfile() {
		return false;
	}

	/**
	 * Returns if the pilot's Water Cooler signature image can be edited.
	 * @return FALSE
	 */
	@Override
	public boolean getCanChangeSignature() {
		return false;
	}
	
	/**
	 * Returns if the Pilot can be transferred to another airline.
	 * @return FALSE
	 */
	@Override
	public boolean getCanTransfer() {
		return false;
	}
	
	/**
	 * Returns if the pilot can be activated.
	 * @return FALSE
	 */
	@Override
	public boolean getCanActivate() {
	   return false;
	}
}