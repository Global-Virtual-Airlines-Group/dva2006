// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.testing.ExamProfile;

import org.deltava.security.SecurityContext;

import org.deltava.util.system.SystemData;

/**
 * An Access Controller for Examination Profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ExamProfileAccessControl extends AccessControl {

	private ExamProfile _ep;

	private boolean _canRead;
	private boolean _canEdit;

	/**
	 * Initialize the Access Controller.
	 * @param ctx the Security Context
	 * @param ep the Examination profile bean
	 */
	public ExamProfileAccessControl(SecurityContext ctx, ExamProfile ep) {
		super(ctx);
		_ep = ep;
	}

	/**
	 * Calculates access rights.
	 */
	public void validate() {
		validateContext();

		// Check if the exam belongs to our airline
		boolean isOurAirline = (_ep == null) || SystemData.get("airline.code").equals(_ep.getOwner().getCode());

		// Check if we are a member of the HR role
		_canEdit = isOurAirline && (_ctx.isUserInRole("HR") || _ctx.isUserInRole("TestAdmin"));
		_canRead = (_ep == null) || _canEdit || _ctx.isUserInRole("Instructor") || _ctx.isUserInRole("Examination");
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
}