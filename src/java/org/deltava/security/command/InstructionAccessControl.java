// Copyright 2006, 2007, 2010, 2016, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.academy.*;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Fleet Academy Instruction sessions.
 * @author Luke
 * @version 11.0
 * @since 1.0
 */

public class InstructionAccessControl extends AccessControl {
	
	private final Instruction _i;
	
	private boolean _canCreate;
	private boolean _canCancel;
	private boolean _canEdit;
	private boolean _canDelete;

	/**
	 * Initializes the Access Controller.
	 * @param ctx the security context
	 * @param i the Instruction bean
	 */
	public InstructionAccessControl(SecurityContext ctx, Instruction i) {
		super(ctx);
		_i = i;
	}

	/**
	 * Calculates access rights.
	 */
	@Override
	public void validate() {
		validateContext();
		
		// Check roles
		boolean isHR = _ctx.isUserInRole("HR");
		boolean isAcademyAdmin = _ctx.isUserInRole("AcademyAdmin");

		// Set create rights
		_canCreate = isHR || isAcademyAdmin || _ctx.isUserInRole("Instructor");
		if ((_i == null) || (!_ctx.isAuthenticated()))
			return;

		// Set access rights
		_canDelete = isHR;
		boolean isOurs = (_ctx.getUser().getID() == _i.getPilotID()) || (_ctx.getUser().getID() == _i.getInstructorID());
		if (_i instanceof InstructionSession is) {
			_canEdit |= _canCreate;
			_canCancel = (isOurs || isHR) && (is.getStatus() == InstructionSession.SCHEDULED);
		} else
			_canEdit = isHR || isAcademyAdmin || (_ctx.getUser().getID() == _i.getInstructorID());	
	}
	
	/**
	 * Returns if the user can create a new Instruction <i>Flight Report</i>.
	 * @return TRUE if the user can create a new flight report, otherwise FALSE
	 */
	public boolean getCanCreate() {
		return _canCreate;
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

	/**
	 * Returns if the user can delete the Instruction Session.
	 * @return TRUE if the user can delete the session, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}