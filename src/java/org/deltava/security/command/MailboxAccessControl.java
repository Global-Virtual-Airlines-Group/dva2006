// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.system.EMailConfiguration;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for mailbox profiles.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class MailboxAccessControl extends AccessControl {

	private EMailConfiguration _cfg;

	private boolean _canEdit;
	private boolean _canDelete;

	/**
	 * Initializes the Access Controller.
	 * @param ctx the command context
	 * @param cfg the IMAP configuration bean
	 */
	public MailboxAccessControl(SecurityContext ctx, EMailConfiguration cfg) {
		super(ctx);
		_cfg = cfg;
	}

	/**
	 * Calculates access rights.
	 */
	public void validate() {
		validateContext();

		if ((_cfg == null) || !_ctx.isAuthenticated())
			return;

		// Calculate access properties
		_canDelete = _ctx.isUserInRole("HR");
		_canEdit = _ctx.isUserInRole("HR");
	}

	/**
	 * Returns if the user can edit the mailbox profile.
	 * @return TRUE if the profile can be edited, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}

	/**
	 * Returns if the user can delete the mailbox profile.
	 * @return TRUE if the profile can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}