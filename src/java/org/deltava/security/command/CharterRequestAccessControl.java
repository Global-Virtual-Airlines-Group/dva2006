// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.assign.CharterRequest;
import org.deltava.beans.assign.CharterRequest.RequestStatus;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Charter flight Requests.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class CharterRequestAccessControl extends AccessControl {
	
	private final CharterRequest _req;
	
	private boolean _canView;
	private boolean _canEdit;
	private boolean _canDispose;

	/**
	 * Initializes the Access Controller.
	 * @param ctx the SecurityContext
	 * @param cr the CharterRequest
	 */
	public CharterRequestAccessControl(SecurityContext ctx, CharterRequest cr) {
		super(ctx);
		_req = cr;
	}

	@Override
	public void validate() {
		validateContext();
		if (!_ctx.isAuthenticated()) return;

		// Handle new entry
		if (_req == null) {
			_canEdit = _ctx.isAuthenticated();
			return;
		}
		
		boolean isOurs = (_req.getAuthorID() == _ctx.getUser().getID());
		boolean hasRole = _ctx.isUserInRole("Operations") || _ctx.isUserInRole("HR");
		_canView = isOurs || hasRole || _ctx.isUserInRole("PIREP");
		_canEdit = isOurs && (_req.getStatus() == RequestStatus.PENDING);
		_canDispose = hasRole && (_req.getStatus() == RequestStatus.PENDING);
	}

	/**
	 * Returns if the user can view this Charter Request.
	 * @return TRUE if the user can view the request, otherwise FALSE
	 */
	public boolean getCanView() {
		return _canView;
	}

	/**
	 * Returns if the user can edit this Charter Request.
	 * @return TRUE if the user can edit the request, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}

	/**
	 * Returns if the user can dispose of this Charter Request.
	 * @return TRUE if the user can dispose of the request, otherwise FALSE
	 */
	public boolean getCanDispose() {
		return _canDispose;
	}
}