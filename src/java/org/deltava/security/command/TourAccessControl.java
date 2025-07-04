// Copyright 2021, 2022, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import java.time.Instant;

import org.deltava.beans.stats.Tour;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Tour beans.
 * @author Luke
 * @version 11.6
 * @since 10.0
 */

public class TourAccessControl extends AccessControl {
	
	private final Tour _t;
	
	private boolean _canCreate;
	private boolean _canRead;
	private boolean _canEdit;
	private boolean _canEditLegs;
	private boolean _canDelete;
	private boolean _canViewFeedback;
	private boolean _canProvideFeedback;

	/**
	 * Initializes the access controller.
	 * @param ctx the command context
	 * @param t the Tour
	 */
	public TourAccessControl(SecurityContext ctx, Tour t) {
		super(ctx);
		_t = t;
	}

	@Override
	public void validate() {
		validateContext();
		
		// Check creation access
		boolean hasRole = _ctx.isUserInRole("Operations") || _ctx.isUserInRole("Event");
		_canCreate = hasRole;
		_canEdit = hasRole;
		_canRead = (_t != null) && (_t.getActive() || hasRole);
		_canEditLegs = (_t == null) ? _canCreate : (_canEdit && _t.getProgress().isEmpty());
		_canDelete = (_t != null) && (_ctx.isUserInRole("Admin") || _canEditLegs);
		
		// Check for feedback
		if (_t != null) {
			_canViewFeedback = hasRole || _ctx.isUserInRole("HR");
			boolean hasFeedback = _ctx.isAuthenticated() && _t.hasFeedback(_ctx.getUser().getID());
			_canProvideFeedback = _ctx.isUserInRole("Pilot") && !hasFeedback && _t.isActiveOn(Instant.now());
		}
	}

	/**
	 * Returns whether the user can create a new Tour.
	 * @return TRUE if the user can create a Tour, otherwise FALSE
	 */
	public boolean getCanCreate() {
		return _canCreate;
	}
	
	/**
	 * Returns whether the user can read a Tour profile.
	 * @return TRUE if the user can read the Tour, otherwise FALSE
	 */
	public boolean getCanRead() {
		return _canRead;
	}
	
	/**
	 * Returns whether the user can edit this Tour metadata.
	 * @return TRUE if the user can edit the Tour, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}
	
	/**
	 * Returns whether the user can modify this Tour's Flight legs.
	 * @return TRUE if the user can edit the legs, otherwise FALSE
	 */
	public boolean getCanEditLegs() {
		return _canEditLegs;
	}
	
	/**
	 * Returns whether the user can delete this Tour.
	 * @return TRUE if the user can delete the Tour, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
	
	/**
	 * Returns whether the User can view feedback about this Tour.
	 * @return TRUE if feedback can be viewed, otherwise FALSE
	 */
	public boolean getCanViewFeedback() {
		return _canViewFeedback;
	}
	
	/**
	 * Returns whether the User can provide feedback about this Tour.
	 * @return TRUE if feedback can be provided, otherwise FALSE
	 */
	public boolean getCanProvideFeedback() {
		return _canProvideFeedback;
	}
}