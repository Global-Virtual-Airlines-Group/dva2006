// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.FlightReport;
import org.deltava.beans.testing.CheckRide;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller
 * @author Luke
 * @version 2.0
 * @since 2.0
 */

public class CrossAppPIREPAccessControl extends PIREPAccessControl {

	private ExamAccessControl _crAccess;
	
	/**
	 * Initializes the access controller.
	 * @param ctx the Command Context
	 * @param fr the Flight Report to be operated on
	 * @param cr the Checkride to evaluate
	 */
	public CrossAppPIREPAccessControl(SecurityContext ctx, FlightReport fr, CheckRide cr) {
		super(ctx, fr);
		_crAccess = new ExamAccessControl(ctx, cr);
	}
	
	/**
	 * Calculates access rights.
	 */
	public void validate() {
		super.validate();
		try {
			_crAccess.validate();
		} catch (AccessControlException ace) {
			// empty
		}
	}

	/**
	 * Returns if a new PIREP can be created.
	 * @return FALSE
	 */
	public boolean getCanCreate() {
		return false;
	}

	/**
	 * Returns if the PIREP can be edited.
	 * @return FALSE
	 */
	public boolean getCanEdit() {
		return false;
	}

	/**
	 * Returns if the PIREP can be submitted.
	 * @return FALSE
	 */
	public boolean getCanSubmit() {
		return false;
	}
	
	/**
	 * Returns if the PIREP can be submitted when editing.
	 * @return FALSE
	 */
	public boolean getCanSubmitIfEdit() {
	   return false;
	}

	/**
	 * Returns if the PIREP can be held.
	 * @return FALSE
	 */
	public boolean getCanHold() {
		return false;
	}
	
	/**
	 * Returns if the PIREP can be released from hold status.
	 * @return FALSE
	 */
	public boolean getCanRelease() {
		return false;
	}

	/**
	 * Returns if the PIREP can be approved <i>and the Check Ride can be scored</i>.
	 * @return TRUE if it can be approved, otherwise FALSE
	 */
	public boolean getCanApprove() {
		return super.getCanApprove() && _crAccess.getCanScore();
	}

	/**
	 * Returns if the PIREP can be rejected.
	 * @return TRUE if it can be rejected, otherwise FALSE
	 */
	public boolean getCanReject() {
		return super.getCanReject() && _crAccess.getCanScore();
	}

	/**
	 * Returns if the PIREP can be deleted.
	 * @return FALSE
	 */
	public boolean getCanDelete() {
		return false;
	}

	/**
	 * Returns if the PIREP was submitted by the current user.
	 * @return FALSE
	 */
	public boolean getOurFlight() {
		return false;
	}
	
	/**
	 * Returns if the PIREP can be disposed of in any way (approved/rejected) by the current user.
	 * @return TRUE if the PIREP can be approved or rejected, otherwise FALSE
	 * @see PIREPAccessControl#getCanApprove()
	 * @see PIREPAccessControl#getCanReject()
	 */
	public boolean getCanDispose() {
		return (super.getCanApprove() || super.getCanReject()) && _crAccess.getCanScore();
	}
}