// Copyright 2007, 2009, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.testing.CheckRide;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Flight Reports across Airlines.
 * @author Luke
 * @version 7.0
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
		_crAccess = new ExamAccessControl(ctx, cr, null);
	}
	
	/**
	 * Calculates access rights.
	 */
	@Override
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
	@Override
	public boolean getCanCreate() {
		return false;
	}

	/**
	 * Returns if the PIREP can be edited.
	 * @return FALSE
	 */
	@Override
	public boolean getCanEdit() {
		return false;
	}

	/**
	 * Returns if the PIREP can be submitted.
	 * @return FALSE
	 */
	@Override
	public boolean getCanSubmit() {
		return false;
	}
	
	/**
	 * Returns if the PIREP can be submitted when editing.
	 * @return FALSE
	 */
	@Override
	public boolean getCanSubmitIfEdit() {
	   return false;
	}

	/**
	 * Returns if the PIREP can be held.
	 * @return FALSE
	 */
	@Override
	public boolean getCanHold() {
		return false;
	}
	
	/**
	 * Returns if the PIREP can be released from hold status.
	 * @return FALSE
	 */
	@Override
	public boolean getCanRelease() {
		return false;
	}

	/**
	 * Returns if the PIREP can be approved <i>and the Check Ride can be scored</i>.
	 * @return TRUE if it can be approved, otherwise FALSE
	 */
	@Override
	public boolean getCanApprove() {
		return super.getCanApprove() && _crAccess.getCanScore();
	}

	/**
	 * Returns if the PIREP can be rejected.
	 * @return TRUE if it can be rejected, otherwise FALSE
	 */
	@Override
	public boolean getCanReject() {
		return super.getCanReject() && _crAccess.getCanScore();
	}

	/**
	 * Returns if the PIREP can be deleted.
	 * @return FALSE
	 */
	@Override
	public boolean getCanDelete() {
		return false;
	}

	/**
	 * Returns if the PIREP was submitted by the current user.
	 * @return FALSE
	 */
	@Override
	public boolean getOurFlight() {
		return false;
	}
	
	/**
	 * Returns if the PIREP can be disposed of in any way (approved/rejected) by the current user.
	 * @return TRUE if the PIREP can be approved or rejected, otherwise FALSE
	 * @see PIREPAccessControl#getCanApprove()
	 * @see PIREPAccessControl#getCanReject()
	 */
	@Override
	public boolean getCanDispose() {
		return (super.getCanApprove() || super.getCanReject()) && _crAccess.getCanScore();
	}
}