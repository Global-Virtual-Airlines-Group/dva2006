// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.hr.*;

import org.deltava.security.SecurityContext;

/**
 * An Access controller for Job Posting applications.
 * @author Luke
 * @version 3.4
 * @since 3.4
 */

public class JobApplicationAccessControl extends AccessControl {
	
	private JobPosting _jp;
	private Application _a;
	
	private boolean _canView;

	/**
	 * Initializes the access controller.
	 * @param ctx the SecurityContext
	 * @param job the JobPosting bean
	 * @param a the Application bean
	 */
	public JobApplicationAccessControl(SecurityContext ctx, JobPosting job, Application a) {
		super(ctx);
		_jp = job;
		_a = a;
	}

	/**
	 * Calculates access rights.
	 */
	@Override
	public void validate() {
		validateContext();
		
		// Check Job posting access
		try {
			JobPostingAccessControl jpa = new JobPostingAccessControl(_ctx, _jp);
			jpa.validate();
			if (!jpa.getCanViewApplicants())
				return;
		} catch (AccessControlException ace) {
			return;
		}
		
		// Check our access
		_canView = _ctx.isUserInRole("HR") || (_a.getStatus() == Application.SHORTLIST) || (_a.getStatus() == Application.APPROVED);
	}
	
	/**
	 * Returns if the user can view this applicaction.
	 * @return TRUE if the application can be viewed, otherwise FALSE
	 */
	public boolean getCanView() {
		return _canView;
	}
}