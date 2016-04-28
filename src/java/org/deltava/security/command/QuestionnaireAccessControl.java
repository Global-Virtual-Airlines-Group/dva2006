// Copyright 2005, 2006, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;

import org.deltava.beans.testing.*;

/**
 * An Access Controller for Applicant Questionnaires.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class QuestionnaireAccessControl extends AccessControl {

	private final Examination _ex;

	private boolean _canRead;
	private boolean _canSubmit;
	private boolean _canScore;

	/**
	 * Initializes the Access Controller.
	 * @param ctx the Command Context
	 * @param ex the Questionnaire
	 */
	public QuestionnaireAccessControl(SecurityContext ctx, Examination ex) {
		super(ctx);
		_ex = ex;
	}

	/**
	 * Calculates access rights.
	 */
	@Override
	public void validate() {

		// Set role values
		boolean isAnonymous = !_ctx.isAuthenticated();
		boolean isExam = _ctx.isUserInRole("Examination");
		boolean isHR = _ctx.isUserInRole("HR");

		// Set status values
		_canSubmit = ((_ex.getStatus() == TestStatus.NEW) && (isAnonymous || isHR));
		_canScore = (_ex.getStatus() == TestStatus.SUBMITTED) && (isExam || isHR);
		_canRead = _canSubmit || _canScore || isHR;
	}

	/**
	 * Returns if the Questionnaire can be read.
	 * @return TRUE if the Questionnaire can be read, otherwise FALSE
	 */
	public boolean getCanRead() {
		return _canRead;
	}

	/**
	 * Returns if the Questionnaire can be submitted.
	 * @return TRUE if the Questionnaire can be submitted, otherwise FALSE
	 */
	public boolean getCanSubmit() {
		return _canSubmit;
	}

	/**
	 * Returns if the Questionnaire can be scored.
	 * @return TRUE if the Questionnaire can be scored, otherwise FALSE
	 */
	public boolean getCanScore() {
		return _canScore;
	}
}