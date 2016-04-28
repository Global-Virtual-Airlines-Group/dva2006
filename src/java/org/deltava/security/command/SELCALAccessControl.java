// Copyright 2005, 2006, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.schedule.SelectCall;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for SELCAL codes.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class SELCALAccessControl extends AccessControl {
	
	private final SelectCall _sc;
	private boolean _forceDeny;
	
	private boolean _canReserve;
	private boolean _canRelease;

	/**
	 * Initializes the Access Controller.
	 * @param ctx the security context
	 * @param sc the SELCAL bean
	 */
	public SELCALAccessControl(SecurityContext ctx, SelectCall sc) {
		super(ctx);
		_sc = sc;
	}

    /**
     * Calculates access rights.
     */
	@Override
	public void validate() {
		if (!_ctx.isAuthenticated() || (_sc == null))
			return;

		// Calculate access rights
		boolean isMine = (_sc.getReservedBy() == _ctx.getUser().getID());
		boolean isHR = _ctx.isUserInRole("HR") || _ctx.isUserInRole("Schedule");
		_canReserve = (_sc.getReservedBy() == 0) && (!_forceDeny);
		_canRelease = isMine || ((_sc.getReservedBy() != 0) && isHR);
	}
	
	/**
	 * Marks all additional SELCAL codes as unavailable.
	 */
	public void markUnavailable() {
		_forceDeny = true;
	}
	
	/**
	 * Returns if the SELCAL code can be reserved.
	 * @return TRUE if the code can be reserved, otherwise FALSE
	 */
	public boolean getCanReserve() {
		return _canReserve;
	}
	
	/**
	 * Returns if the SELCAL code can be released.
	 * @return TRUE if the code can be released, otherwise FALSE
	 */
	public boolean getCanRelease() {
		return _canRelease;
	}
}