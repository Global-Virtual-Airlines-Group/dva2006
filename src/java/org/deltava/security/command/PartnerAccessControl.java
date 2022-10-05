// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.PartnerInfo;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for virtual airline Partner data.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class PartnerAccessControl extends AccessControl {
	
	private final PartnerInfo _pi;
	
	private boolean _canCreate;
	private boolean _canEdit;
	private boolean _canDelete;

	/**
	 * Initializes the access controller.
	 * @param ctx the SecurityContext
	 * @param pi the PartnerInfo bean, or null
	 */
	public PartnerAccessControl(SecurityContext ctx, PartnerInfo pi) {
		super(ctx);
		_pi = pi;
	}

	@Override
	public void validate() throws AccessControlException {
		validateContext();

		_canCreate = _ctx.isUserInRole("Operations");
		_canEdit = _ctx.isUserInRole("Operations") && (_pi != null);
		_canDelete = _ctx.isUserInRole("Admin") && (_pi != null);
	}

	/**
	 * Returns if the user can create a new Partner Information entry.
	 * @return TRUE if an entry can be created, otherwise FALSE
	 */
	public boolean getCanCreate() {
		return _canCreate;
	}
	
	/**
	 * Returns if the user can edit the Partner Information entry.
	 * @return TRUE if the entry can be edited, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}

	/**
	 * Returns if the user can delete the Partner Information entry.
	 * @return TRUE if the entry can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}