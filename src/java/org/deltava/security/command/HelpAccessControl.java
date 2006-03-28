// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Online Help Entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class HelpAccessControl extends AccessControl {
	
	private boolean _canEdit;

	/**
	 * Initializes the Access Controller.
	 * @param ctx the security context
	 */
	public HelpAccessControl(SecurityContext ctx) {
		super(ctx);
	}

	/**
	 * Calculates access rights.
	 */
	public void validate() {
		validateContext();
		_canEdit = _ctx.isUserInRole("HR") || _ctx.isUserInRole("PIREP") || _ctx.isUserInRole("Examination") || _ctx.isUserInRole("Developer"); 
	}

	/**
	 * Returns if the Online Help Entry can be updated.
	 * @return TRUE if the entry can be updated, otherwise FALSE 
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}
}