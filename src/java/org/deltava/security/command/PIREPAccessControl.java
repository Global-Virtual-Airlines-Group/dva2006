// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;
import org.deltava.commands.CommandSecurityException;

import org.deltava.beans.FlightReport;

/**
 * An access controller for PIREP operations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public final class PIREPAccessControl extends AccessControl {

	private FlightReport _pirep;

	private boolean _ourPIREP;
	private boolean _canCreate;
	private boolean _canEdit;
	private boolean _canSubmit;
	private boolean _canHold;
	private boolean _canApprove;
	private boolean _canReject;
	private boolean _canDelete;

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
	 * @throws CommandSecurityException never
	 */
	public void validate() throws CommandSecurityException {
		validateContext();

		// Get PIREP creation access, and abort if no PIREP provided
		_canCreate = _ctx.isUserInRole("Pilot");
		if (_pirep == null) {
			_canSubmit = _canCreate;
			return;
		}

		// Set role variables
		int status = _pirep.getStatus();
		boolean isHR = _ctx.isUserInRole("HR");
		boolean isPirep = _ctx.isUserInRole("PIREP");
		_ourPIREP = _ctx.isAuthenticated() && (_pirep.getDatabaseID(FlightReport.DBID_PILOT) == _ctx.getUser().getID());

		// Check if we can submit/hold/approve/reject/edit the PIREP
		_canSubmit = (status == FlightReport.DRAFT) && (_ourPIREP || isPirep || isHR);
		_canHold = ((isPirep || isHR) && (status == FlightReport.SUBMITTED));
		_canApprove = ((isPirep || isHR) && ((status == FlightReport.SUBMITTED) || (status == FlightReport.HOLD)) || (isHR && (status == FlightReport.REJECTED)));

		_canReject = (status != FlightReport.REJECTED) && (_canApprove || (isHR && (status == FlightReport.OK)));
		_canEdit = (_canSubmit || _canHold || _canApprove || _canReject);
		_canDelete = (_ourPIREP && (status == FlightReport.DRAFT)) || ((_pirep.getDatabaseID(FlightReport.DBID_ASSIGN) == 0) && _ctx.isUserInRole("Admin"));
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
	 * @return TRUE if it can be submitted, otherwise FALSE
	 */
	public boolean getCanSubmit() {
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
	 * Returns if the PIREP was submitted by the current user.
	 * @return TRUE if it the current created this PIREP, otherwise FALSE
	 */
	public boolean getOurFlight() {
		return _ourPIREP;
	}
}