// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Elite program operations.
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

public class EliteAccessControl extends AccessControl {
	
	private boolean _canEdit;
	private boolean _canDelete;
	private boolean _canRecalc;

	/**
	 * Initializes the Access Controller.
	 * @param ctx the security context
	 */
	public EliteAccessControl(SecurityContext ctx) {
		super(ctx);
	}

	@Override
	public void validate() throws AccessControlException {
		validateContext();
		
		_canEdit = _ctx.isUserInRole("Operations");
		_canDelete = _ctx.isUserInRole("Admin");
		_canRecalc = _canEdit || _ctx.isUserInRole("PIREP") || _ctx.isUserInRole("HR");
	}

	/**
	 * Returns whether an EliteLevel can be edited.
	 * @return TRUE if editable, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}
	
	/**
	 * Returns whether an EliteLevel can be deleted.
	 * @return TRUE if deletable, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
	
	/**
	 * Returns whether a Pilot's EliteStatus can be recalculated.
	 * @return TRUE if it can be recalculated, otherwise FALSE
	 */
	public boolean getCanRecalculate() {
		return _canRecalc;
	}
}