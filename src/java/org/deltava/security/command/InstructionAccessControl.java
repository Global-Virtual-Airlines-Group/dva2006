// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.academy.*;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Fleet Academy Instruction sessions.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InstructionAccessControl extends AccessControl {
	
	private InstructionSession _is;
	
	private boolean _canCancel;
	private boolean _canEdit;

	/**
	 * Initializes the Access Controller.
	 * @param ctx the security context
	 * @param s the InstructionSession bean
	 */
	public InstructionAccessControl(SecurityContext ctx, InstructionSession s) {
		super(ctx);
		_is = s;
	}

	/**
	 * Calculates access rights.
	 */
	public void validate() {
		validateContext();
		if ((_is == null) || (!_ctx.isAuthenticated()))
			return;
		
		// Check roles
		boolean isHR = _ctx.isUserInRole("HR");
		boolean isOurs = (_ctx.getUser().getID() == _is.getPilotID()) || (_ctx.getUser().getID() == _is.getInstructorID());
		
		// Set access rights
		_canCancel = (isOurs || isHR) && (_is.getStatus() == InstructionSession.SCHEDULED);
		_canEdit = isHR || (_ctx.getUser().getID() == _is.getInstructorID());
	}

	/**
	 * Returns if the user can cancel the Instruction Session.
	 * @return TRUE if the user can cancel the session, otherwise FALSE
	 */
	public boolean getCanCancel() {
		return _canCancel;
	}
	
	/**
	 * Returns if the user can edit the Instruction Session.
	 * @return TRUE if the user can edit the session, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}
}