// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.commands.CommandSecurityException;
import org.deltava.security.SecurityContext;

import org.deltava.beans.testing.Test;
import org.deltava.beans.testing.Examination;

/**
 * An Access Controller for Applicant Questionnaires.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class QuestionnaireAccessControl extends AccessControl {

	private Examination _ex;

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
	 * @throws CommandSecurityException never
	 */
	public void validate() throws CommandSecurityException {

		// Set role values
		boolean isAnonymous = !_ctx.isAuthenticated();
		boolean isExam = _ctx.isUserInRole("Examination");
		boolean isHR = _ctx.isUserInRole("HR");

		// Set status values
		_canSubmit = ((_ex.getStatus() == Test.NEW) && (isAnonymous || isHR));
		_canScore = (_ex.getStatus() == Test.SUBMITTED) && (isExam || isHR);
		_canRead = _canSubmit || _canScore;
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