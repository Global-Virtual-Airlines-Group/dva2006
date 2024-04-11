// Copyright 2005, 2006, 2007, 2009, 2010, 2011, 2012, 2014, 2016, 2018, 2019, 2021, 2022, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import java.time.*;

import org.deltava.beans.ExternalID;
import org.deltava.beans.acars.Restriction;
import org.deltava.beans.flight.*;

import org.deltava.security.SecurityContext;

/**
 * An access controller for Flight Report operations.
 * @author Luke
 * @version 11.2
 * @since 1.0
 */

public class PIREPAccessControl extends AccessControl {

	protected final FlightReport _pirep;

	private boolean _ourPIREP;
	private boolean _canCreate;
	private boolean _canEdit;
	private boolean _canCalculateLoad;
	private boolean _canSubmit;
	private boolean _canHold;
	private boolean _canRelease;
	private boolean _canApprove;
	private boolean _canReject;
	private boolean _canDelete;
	private boolean _canOverrideDateRange;
	private boolean _canViewDiagData;
	private boolean _canViewComments;
	private boolean _canUpdateComments;
	private boolean _canPreApprove;
	private boolean _canProxySubmit;
	private boolean _canAdjustEvents;
	private boolean _canUseSimBrief;
	private boolean _canViewSimBrief;
	private boolean _canEliteRescore;

	/**
	 * Initializes the controller.
	 * @param ctx the Command Context
	 * @param fr the Flight Report to be operated on
	 */
	public PIREPAccessControl(SecurityContext ctx, FlightReport fr) {
		super(ctx);
		_pirep = fr;
	}

	@Override
	public void validate() {
		validateContext();
		
		// Check if we can submit non-ACARS PIREPs
		boolean noManual = _ctx.isAuthenticated() && (_ctx.getUser().getACARSRestriction() == Restriction.NOMANUAL);

		// Get PIREP creation access, and abort if no PIREP provided
		final boolean isHR = _ctx.isUserInRole("HR") || _ctx.isUserInRole("Operations");
		_canCreate = _ctx.isUserInRole("Pilot") && !noManual;
		_canPreApprove = isHR;
		if (_pirep == null) {
			_canSubmit = _canCreate;
			return;
		}
		
		// Set role variables
		final FlightStatus status = _pirep.getStatus();
		final boolean isAcademy = _ctx.isUserInRole("Instructor") || _ctx.isUserInRole("AcademyAdmin");
		final boolean isPirep = _pirep.hasAttribute(FlightReport.ATTR_ACADEMY) ? isAcademy : _ctx.isUserInRole("PIREP");
		_ourPIREP = _ctx.isAuthenticated() && (_pirep.getDatabaseID(DatabaseID.PILOT) == _ctx.getUser().getID());
		_canOverrideDateRange = _ctx.isUserInRole("PIREP") && !_ourPIREP;
		
		// Set status variables
		final boolean isDraft = (status == FlightStatus.DRAFT);
		final boolean isSubmitted = (status == FlightStatus.SUBMITTED);
		final boolean isRejected = (status == FlightStatus.REJECTED);
		final boolean isHeld = (status == FlightStatus.HOLD);

		// Check if held by us
		final int disposalID = _pirep.getDatabaseID(DatabaseID.DISPOSAL);
		final boolean isDisposedByMe = _ctx.isAuthenticated() && (disposalID == _ctx.getUser().getID());
		final boolean isHeldByMe = (isHeld && _ctx.isAuthenticated() && isDisposedByMe || (disposalID == 0));
		final boolean canReleaseHold = !isHeld || isHR || isHeldByMe;
		
		// Check if we can calculate load factor
		Instant today = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
		boolean isPast = _pirep.getDate().isBefore(today);
		_canCalculateLoad = isDraft && _ourPIREP && (isPast || (_pirep.getLoadFactor() <= 0));
		
		// Check if we can submit/hold/approve/reject/edit the PIREP
		_canSubmit = isDraft && (_ourPIREP || isPirep || isHR) && !noManual;
		_canHold = (isSubmitted && (isPirep || isHR)) || ((status == FlightStatus.OK) && isHR);
		_canApprove = ((isPirep || isHR) && canReleaseHold && (isSubmitted || (status == FlightStatus.HOLD)) || (isHR && isRejected));
		_canReject = !isRejected && canReleaseHold && (_canApprove || (isHR && (status == FlightStatus.OK)));
		_canRelease = (isHeld && _ctx.isAuthenticated() && canReleaseHold);
		_canEdit = _canSubmit || _canHold || _canApprove || _canReject || _canRelease;
		_canViewDiagData = _ourPIREP || _ctx.isUserInRole("Operations") || _ctx.isUserInRole("Developer");
		_canViewComments = isHR || isPirep || _ourPIREP;
		_canUpdateComments = (isHR || isDisposedByMe) && (isRejected || isHeld || (status == FlightStatus.OK));
		_canProxySubmit = isHR;
		_canAdjustEvents = _canApprove || _canReject || _canHold /*isPirep && !_ourPIREP && !isDraft && (_ctx.isUserInRole("Event") || isHR) */;
		_canUseSimBrief = isDraft && _ourPIREP && _ctx.getUser().hasID(ExternalID.NAVIGRAPH);
		_canViewSimBrief = (_ourPIREP || isPirep) && _pirep.hasAttribute(FlightReport.ATTR_SIMBRIEF);
		_canEliteRescore = (status == FlightStatus.OK) && _ctx.isUserInRole("Operations");
		
		// Get the flight assignment ID
		final boolean isCheckRide = _pirep.hasAttribute(FlightReport.ATTR_CHECKRIDE);
		final boolean isAssigned = (_pirep.getDatabaseID(DatabaseID.ASSIGN) > 0);
		_canDelete = (_ourPIREP && !isAssigned && !isCheckRide && (isDraft || isSubmitted)) || (_ctx.isUserInRole("Admin") && ((isRejected && isAssigned) || !isAssigned));
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
	 * Returns if the load factor for this PIREP can be calculated in advance.
	 * @return TRUE if can be calculated (draft, ours, load factor not set), otherwise FALSE
	 */
	public boolean getCanCalculateLoad() {
		return _canCalculateLoad;
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
	 * Returns whether the disposition comments can be updated.
	 * @return TRUE if the comments can be updated, otherwise FALSE
	 */
	public boolean getCanUpdateComments() {
		return _canUpdateComments;
	}
	
	/**
	 * Returns whether the ACARS diagnostic data can be viewed.
	 * @return TRUE if the diagnostic data can eb viewed, otherwise FALSE
	 */
	public boolean getCanViewDiagnosticData() {
		return _canViewDiagData;
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
	
	/**
	 * Returns if the user can submit a Flight Report on behalf of another user.
	 * @return TRUE if the flight can be submitted on their behalf, otherwise FALSE
	 */
	public boolean getCanProxySubmit() {
		return _canProxySubmit;
	}
	
	/**
	 * Returns if the user can override date range limitations for PIREP submission.
	 * @return TRUE if the flight can be submitted outside the allowed date range, otherwise FALSE
	 */
	public boolean getCanOverrideDateRange() {
		return _canOverrideDateRange;
	}
	
	/**
	 * Returns if the user can adjust Tour/Online Event flags for this PIREP.
	 * @return TRUE if Event/Tour credit can be adjusted, otherwise FALSE
	 */
	public boolean getCanAdjustEvents() {
		return _canAdjustEvents;
	}

	/**
	 * Returns if the user can plot this flight using SimBrief.
	 * @return TRUE if SimBrief can be invoked, otherwise FALSE
	 */
	public boolean getCanUseSimBrief() {
		return _canUseSimBrief;
	}
	
	/**
	 * Returns if the user can view the flight's SimBrief briefing data.
	 * @return TRUE if the SimBrief data can be viewed, otherwise FALSE
	 */
	public boolean getCanViewSimBrief() {
		return _canViewSimBrief;
	}
	
	/**
	 * Returns if the user can recalculate the Elite program score for this Flight Report.
	 * @return TRUE if Elite score can be recalculated, otherwise FALSE
	 */
	public boolean getCanEliteRescore() {
		return _canEliteRescore;
	}
}