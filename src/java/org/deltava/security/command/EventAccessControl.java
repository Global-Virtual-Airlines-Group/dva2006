// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import java.util.Iterator;

import org.deltava.beans.Pilot;
import org.deltava.beans.event.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.security.SecurityContext;
import org.deltava.util.system.SystemData;

/**
 * An Access Controller for Online Events.
 * @author Luke
 * @version 2.4
 * @since 1.0
 */

public class EventAccessControl extends AccessControl {

	private Event _ev;

	private boolean _canCreate;
	private boolean _canSignup;
	private boolean _canEdit;
	private boolean _canAddPlan;
	private boolean _canBalance;
	private boolean _canAssignFlights;
	private boolean _canCancel;
	private boolean _canDelete;

	/**
	 * Initializes the Access Controller.
	 * @param ctx the Command context
	 * @param e the Online Event
	 */
	public EventAccessControl(SecurityContext ctx, Event e) {
		super(ctx);
		_ev = e;
	}

	/**
	 * Calculates access rights.
	 */
	public void validate() {
		validateContext();

		// Do nothing if not a Pilot
		if (!_ctx.isUserInRole("Pilot"))
			return;

		// Check creation access
		_canCreate = _ctx.isUserInRole("Event");
		if (_ev == null)
			return;

		// Check if we have a network address
		Pilot p = (Pilot) _ctx.getUser();
		boolean hasID = p.hasNetworkID(_ev.getNetwork());
		boolean isOurs = _ev.getOwner().getCode().equals(SystemData.get("airline.code"));
		
		// Check if we can participate
		boolean canParticipate = isOurs;
		for (Iterator<AirlineInformation> i = _ev.getAirlines().iterator(); !canParticipate && i.hasNext(); ) {
			AirlineInformation ai = i.next();
			canParticipate |= ai.getCode().equals(SystemData.get("airline.code"));
		}

		// Check if any routes stil have signups
		boolean isRouteAvailable = !_ev.getActiveRoutes().isEmpty();

		// Set access variables
		boolean isEvent = (_ctx.isUserInRole("Event") && isOurs) || _ctx.isUserInRole("Admin");
		boolean hasSignups = (!_ev.getSignups().isEmpty());
		_canSignup = (_ev.getStatus() == Event.OPEN) && _ev.getCanSignup() && hasID && isRouteAvailable
				&& canParticipate && (!_ev.isSignedUp(_ctx.getUser().getID()));
		_canAddPlan = ((_ev.getStatus() == Event.OPEN) || (_ev.getStatus() == Event.CLOSED)) && isEvent;
		_canEdit = (_ev.getStatus() != Event.COMPLETE) && isEvent;
		_canBalance = ((_ev.getStatus() == Event.OPEN) || (_ev.getStatus() == Event.CLOSED)) && hasSignups 
			&& isEvent && (_ev.getRoutes().size() > 1);
		_canAssignFlights = ((_ev.getStatus() == Event.CLOSED) || (_ev.getStatus() == Event.ACTIVE)) && hasSignups
				&& isEvent;
		_canCancel = _canEdit;
		_canDelete = isEvent && !hasSignups && (_ev.getStartTime() != null) && (_ev.getStartTime().getTime() > System.currentTimeMillis());
	}

	/**
	 * Returns if a new Online Event can be created.
	 * @return TRUE if a new Online Event can be created, otherwise FALSE
	 */
	public boolean getCanCreate() {
		return _canCreate;
	}

	/**
	 * Returns if a new Signup for this Online Event can be created.
	 * @return TRUE if a Signup for this Online Event can be created, otherwise FALSE
	 */
	public boolean getCanSignup() {
		return _canSignup;
	}

	/**
	 * Returns if this Online Event can be edited.
	 * @return TRUE if the Online Event can be edited, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}

	/**
	 * Returns if a Flight Plan can be added to this Online Event.
	 * @return TRUE if a Flight Plan can be added, otherwise FALSE
	 */
	public boolean getCanAddPlan() {
		return _canAddPlan;
	}
	
	/**
	 * Returns if Signups can be balanced for this Online Event.
	 * @return TRUE if signups can be balanced, otherwise FALSE
	 */
	public boolean getCanBalance() {
		return _canBalance;
	}

	/**
	 * Returns if Flight Assignments can be made for this Online Event.
	 * @return TRUE if Assignments can be made, otherwise FALSE
	 */
	public boolean getCanAssignFlights() {
		return _canAssignFlights;
	}

	/**
	 * Returns if this Online Event can be canceled.
	 * @return TRUE if the Online Event can be canceled, otherwise FALSE
	 */
	public boolean getCanCancel() {
		return _canCancel;
	}
	
	/**
	 * Returns if this Online Event can be deleted.
	 * @return TRUE if the Online Event can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}