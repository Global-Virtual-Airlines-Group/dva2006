// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.system.MessageTemplate;

import org.deltava.security.SecurityContext;
import org.deltava.commands.CommandSecurityException;

/**
 * An Access Controller for Message Templates.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public final class MessageAccessControl extends AccessControl {
	
	private MessageTemplate _mt;
	
	private boolean _canCreate;
	private boolean _canEdit;
	private boolean _canDelete;
	
	/**
	 * Initialize the Access Controller.
	 * @param ctx the Security context
	 */
	public MessageAccessControl(SecurityContext ctx, MessageTemplate mt) {
		super(ctx);
		_mt = mt;
	}

	/**
	 * Check's a User's access.
	 * @throws CommandSecurityException never
	 */
	public void validate() throws CommandSecurityException {
		
		// Set status flags
		_canCreate = _ctx.isUserInRole("HR") || _ctx.isUserInRole("Developer");
		_canEdit = _canCreate;
		
		// Check if we can delete this message template
		_canDelete = (_ctx.isUserInRole("Admin") && (_mt != null));
	}

	/**
	 * Returns if the user can create a new Message Template.
	 * @return TRUE if a Message Template can be created, otherwise FALSE
	 */
	public boolean getCanCreate() {
		return _canCreate;
	}

	/**
	 * Returns if the user can edit this Message Template.
	 * @return TRUE if a Message Template can be edited, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}
	
	/**
	 * Returns if the user can delete this Message Template.
	 * @return TRUE if this Message Template can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}