// Copyright 2012, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.system.AirlineInformation;

import org.deltava.security.SecurityContext;

/**
 * An access controller for Virtual Airlines application profiles.
 * @author Luke
 * @version 8.6
 * @since 5.0
 */

public class AirlineInformationAccessControl extends AccessControl {

	private final AirlineInformation _aInfo;
	
	private boolean _canRead;
	private boolean _canEdit;
	
	/**
	 * Creates the Access Controller.
	 * @param ai the AirlineInformation bean
	 * @param ctx the SecurityContext
	 */
	public AirlineInformationAccessControl(AirlineInformation ai, SecurityContext ctx) {
		super(ctx);
		_aInfo = ai;
	}

	/**
	 * Calculates access rights.
	 */
	@Override
	public void validate() {
		validateContext();
		
		// Detect our airline
		if (!_ctx.isAuthenticated()) return;
		boolean isOurs = _ctx.getUser().getPilotCode().startsWith(_aInfo.getCode());
		
		_canRead =  _ctx.isUserInRole("HR");
		_canEdit = isOurs && _ctx.isUserInRole("Admin"); 
	}
	
	/**
	 * Returns whether the profile can be read.
	 * @return TRUE if readable, otherwise FALSE
	 */
	public boolean getCanRead() {
		return _canRead;
	}
	
	/**
	 * Returns whether the profile can be edited.
	 * @return TRUE if editable, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}
}