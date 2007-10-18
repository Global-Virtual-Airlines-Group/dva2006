// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.testing.QuestionProfile;

import org.deltava.security.SecurityContext;

import org.deltava.util.system.SystemData;

/**
 * An Access Controller for Examination Question Profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class QuestionProfileAccessControl extends AccessControl {

	private QuestionProfile _qp;

	private boolean _canRead;
	private boolean _canInclude;
	private boolean _canEdit;
	private boolean _canDelete;

	/**
	 * Initializes the Access Controller.
	 * @param ctx the command context
	 */
	public QuestionProfileAccessControl(SecurityContext ctx, QuestionProfile qp) {
		super(ctx);
		_qp = qp;
	}

	/**
	 * Calculates access rights.
	 * @throws AccessControlException if the user cannot read the profile
	 */
	public void validate() throws AccessControlException {
		validateContext();

		// Calculate access variables
		boolean isHR = _ctx.isUserInRole("HR");
		boolean isOurs = (_qp == null) || SystemData.get("airline.code").equals(_qp.getOwner().getCode());

		_canRead = isHR || _ctx.isUserInRole("Examination") || _ctx.isUserInRole("TestAdmin");
		_canInclude = (_qp != null) && _qp.getAirlines().contains(SystemData.getApp(SystemData.get("airline.code"))) && (isHR || _ctx.isUserInRole("TestAdmin"));
		_canEdit = isOurs && _canInclude;
		_canDelete = isHR && isOurs && (_qp != null) && (_qp.getTotalAnswers() == 0);
		if (!_canRead)
			throw new AccessControlException("Cannot view Question Profile");
	}

	/**
	 * Returns if the profile can be viewed.
	 * @return TRUE if it can be viewed, otherwise FALSE
	 */
	public boolean getCanRead() {
		return _canRead;
	}

	/**
	 * Returns if the profile can be edited.
	 * @return TRUE if it can be edited, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}
	
	/**
	 * Returns if the profile can be included in an Examination.
	 * @return TRUE if it can be included, otherwise FALSE
	 */
	public boolean getCanInclude() {
		return _canInclude;
	}

	/**
	 * Returns if the profile can be deleted.
	 * @return TRUE if it can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}