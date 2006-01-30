// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.event.*;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Online Event Flight Plans.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FlightPlanAccessControl extends AccessControl {

	private Event _ev;
	
	private boolean _canCreate;
	private boolean _canDelete;
	
	/**
	 * Initialize the Access Controller.
	 * @param ctx the Command context
	 * @param e the Online Event
	 */
	public FlightPlanAccessControl(SecurityContext ctx, Event e) {
		super(ctx);
		_ev = e;
	}

	/**
	 * Calculates access level.
	 */
	public void validate() {
		validateContext();

		// Do nothing if no event defined
		if (_ev == null)
			return;
		
		// Calculate event state
		boolean isEventOK = (_ev.getStatus() == Event.ACTIVE) || (_ev.getStatus() == Event.CLOSED);

		// Set access rights
		_canCreate = isEventOK && _ctx.isUserInRole("Event");
		_canDelete = _canCreate;
	}

	/**
	 * Returns if a new Flight Plan can be created for this Online Event.
	 * @return TRUE if a new Flight Plan can be created, otherwise FALSE
	 */
	public boolean getCanCreate() {
		return _canCreate;
	}
	
	/**
	 * Returns if this Flight Plan can be deleted.
	 * @return TRUE if the Flight Plan can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}