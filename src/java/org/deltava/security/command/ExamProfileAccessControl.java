// Copyright 2005, 2006, 2007, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.testing.ExamProfile;

import org.deltava.security.SecurityContext;

import org.deltava.util.system.SystemData;

/**
 * An Access Controller for Examination Profiles.
 * @author Luke
 * @version 9.1
 * @since 1.0
 */

public class ExamProfileAccessControl extends AccessControl {

	private final ExamProfile _ep;

	private boolean _canRead;
	private boolean _canCreate;
	private boolean _canEdit;
	private boolean _canDelete;

	/**
	 * Initialize the Access Controller.
	 * @param ctx the Security Context
	 * @param ep the Examination profile bean
	 */
	public ExamProfileAccessControl(SecurityContext ctx, ExamProfile ep) {
		super(ctx);
		_ep = ep;
	}

	@Override
	public void validate() {
		validateContext();

		// Check create access
		boolean isHR = _ctx.isUserInRole("HR");
		_canCreate = isHR || _ctx.isUserInRole("AcademyAdmin") || _ctx.isUserInRole("TestAdmin");
		if (_ep == null)
			return;

		// Check if the exam belongs to our airline
		boolean isOurAirline = SystemData.get("airline.code").equals(_ep.getOwner().getCode());
		boolean isAdmin = _ep.getAcademy() ? _ctx.isUserInRole("AcademyAdmin") : _ctx.isUserInRole("TestAdmin");
		boolean isExaminer = _ep.getAcademy() ? _ctx.isUserInRole("Instructor") : _ctx.isUserInRole("Examination");

		// Check if we are a member of the HR role
		_canEdit = isOurAirline && (isHR || isAdmin);
		_canRead = _canEdit || isExaminer || (_ep.getAcademy() && _ctx.isUserInRole("AcademyAudit"));
		_canDelete = _canEdit && (_ep.getTotal() == 0);
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
	 * Returns if a new Examination Profile can be created.
	 * @return TRUE if it can be created, otherwise FALSE
	 */
	public boolean getCanCreate() {
		return _canCreate;
	}
	
	/**
	 * Returns if the profile can be deleted.
	 * @return TRUE if it can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}