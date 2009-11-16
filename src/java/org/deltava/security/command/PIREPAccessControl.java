// Copyright 2005, 2006, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.*;
import org.deltava.beans.flight.FlightReport;

import org.deltava.security.SecurityContext;

/**
 * An access controller for Flight Report operations.
 * @author Luke
 * @version 2.7
 * @since 1.0
 */

public class PIREPAccessControl extends AccessControl {

	protected FlightReport _pirep;

	private boolean _ourPIREP;
	private boolean _canCreate;
	private boolean _canEdit;
	private boolean _canSubmit;
	private boolean _canHold;
	private boolean _canRelease;
	private boolean _canApprove;
	private boolean _canReject;
	private boolean _canDelete;
	private boolean _canViewComments;
	private boolean _canPreApprove;

	/**
	 * Initializes the controller.
	 * @param ctx the Command Context
	 * @param fr the Flight Report to be operated on
	 */
	public PIREPAccessControl(SecurityContext ctx, FlightReport fr) {
		super(ctx);
		_pirep = fr;
	}

	/**
	 * Calculates access rights.
	 */
	public void validate() {
		validateContext();
		
		// Check if we can submit non-ACARS PIREPs
		boolean noManual = false;
		if (_ctx.isUserInRole("Pilot")) {
			Pilot usr = (Pilot) _ctx.getUser();
			noManual = (usr.getACARSRestriction() == Pilot.ACARS_ONLY);
		}

		// Get PIREP creation access, and abort if no PIREP provided
		final boolean isHR = _ctx.isUserInRole("HR") || _ctx.isUserInRole("Operations");
		_canCreate = _ctx.isUserInRole("Pilot") && !noManual;
		_canPreApprove = isHR;
		if (_pirep == null) {
			_canSubmit = _canCreate;
			return;
		}
		
		// Set role variables
		final int status = _pirep.getStatus();
		final boolean isPirep = _ctx.isUserInRole("PIREP");
		_ourPIREP = _ctx.isAuthenticated() && (_pirep.getDatabaseID(FlightReport.DBID_PILOT) == _ctx.getUser().getID());
		
		// Set status variables
		final boolean isDraft = (status == FlightReport.DRAFT);
		final boolean isSubmitted = (status == FlightReport.SUBMITTED);
		final boolean isRejected = (status == FlightReport.REJECTED);
		final boolean isHeld = (status == FlightReport.HOLD);

		// Check if held by us
		final boolean isHeldByMe = (isHeld && _ctx.isAuthenticated() && (_pirep.getDatabaseID(FlightReport.DBID_DISPOSAL) == _ctx.getUser().getID()));
		final boolean canReleaseHold = !isHeld || isHR || isHeldByMe;

		// Check if we can submit/hold/approve/reject/edit the PIREP
		_canSubmit = isDraft && (_ourPIREP || isPirep || isHR) && !noManual;
		_canHold = (isSubmitted && (isPirep || isHR)) || ((status == FlightReport.OK) && isHR);
		_canApprove = ((isPirep || isHR) && canReleaseHold && (isSubmitted || (status == FlightReport.HOLD)) || (isHR && isRejected));
		_canReject = !isRejected && canReleaseHold && (_canApprove || (isHR && (status == FlightReport.OK)));
		_canRelease = (isHeld && _ctx.isAuthenticated() && canReleaseHold);
		_canEdit = _canSubmit || _canHold || _canApprove || _canReject || _canRelease;
		_canViewComments = isHR || isPirep || _ourPIREP;
		
		// Get the flight assignment ID
		final boolean isCheckRide = _pirep.hasAttribute(FlightReport.ATTR_CHECKRIDE);
		final boolean isAssigned = (_pirep.getDatabaseID(FlightReport.DBID_ASSIGN) > 0);
		_canDelete = (_ourPIREP && !isAssigned && !isCheckRide && (isDraft || isSubmitted)) || (_ctx.isUserInRole("Admin") && 
				((isRejected && isAssigned) || !isAssigned));
	}

	/**
	 * Returns if a new PIREP can be created.
	 * @return TRUE if it can be created, otherwise FALSE
	 */
	public boolean getCanCreate() {
		return _canCreate;
	}

	/**
	 * Returns if the PIREP can be edited.
	 * @return TRUE if it can be edited, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}

	/**
	 * Returns if the PIREP can be submitted.
	 * @return TRUE if it can be submitted and the length is non-zero, otherwise FALSE
	 * @see PIREPAccessControl#getCanSubmitIfEdit()
	 * @see FlightReport#getLength()
	 */
	public boolean getCanSubmit() {
		return _canSubmit && (_pirep != null) && (_pirep.getLength() > 0);
	}
	
	/**
	 * Returns if the PIREP can be submitted when editing. This returns the same value as canSubmit, except
	 * it does not check if the PIREP time is greater than zero. This allows us to edit a draft 0 length PIREP, and
	 * still have the submit button available.
	 * @return TRUE if it can be submitted, otherwise FALSE
	 * @see PIREPAccessControl#getCanSubmit()
	 */
	public boolean getCanSubmitIfEdit() {
	   return _canSubmit;
	}

	/**
	 * Returns if the PIREP can be held.
	 * @return TRUE if it can be held, otherwise FALSE
	 */
	public boolean getCanHold() {
		return _canHold;
	}
	
	/**
	 * Returns if the PIREP can be released from hold status.
	 * @return TRUE if it can be released, otherwise FALSE
	 */
	public boolean getCanRelease() {
		return _canRelease;
	}

	/**
	 * Returns if the PIREP can be approved.
	 * @return TRUE if it can be approved, otherwise FALSE
	 */
	public boolean getCanApprove() {
		return _canApprove;
	}

	/**
	 * Returns if the PIREP can be rejected.
	 * @return TRUE if it can be rejected, otherwise FALSE
	 */
	public boolean getCanReject() {
		return _canReject;
	}

	/**
	 * Returns if the PIREP can be deleted.
	 * @return TRUE if it can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
	
	/**
	 * Returns whether the disposition comments can be viewed.
	 * @return TRUE if the comments can be viewed, otherwise FALSE
	 */
	public boolean getCanViewComments() {
		return _canViewComments;
	}

	/**
	 * Returns if the PIREP was submitted by the current user.
	 * @return TRUE if it the current created this PIREP, otherwise FALSE
	 */
	public boolean getOurFlight() {
		return _ourPIREP;
	}
	
	/**
	 * Returns if the PIREP can be disposed of in any way (approved/rejected/held) by the current user.
	 * @return TRUE if the PIREP can be approved, rejected or held, otherwise FALSE
	 * @see PIREPAccessControl#getCanApprove()
	 * @see PIREPAccessControl#getCanHold()
	 * @see PIREPAccessControl#getCanReject()
	 */
	public boolean getCanDispose() {
		return _canApprove || _canReject || _canHold;
	}
	
	/**
	 * Returns if the user can pre-approve a Charter Flight.
	 * @return TRUE if a flight can be pre-approved, otherwise FALSE
	 */
	public boolean getCanPreApprove() {
		return _canPreApprove;
	}
}