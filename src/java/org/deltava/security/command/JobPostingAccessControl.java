// Copyright 2010, 2011, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import java.time.Instant;
import java.util.*;

import org.deltava.beans.Pilot;
import org.deltava.beans.hr.*;

import org.deltava.security.SecurityContext;

import org.deltava.util.*;

/**
 * An access controller for Job Postings.
 * @author Luke
 * @version 8.6
 * @since 3.4
 */

public class JobPostingAccessControl extends AccessControl {
	
	private static final List<String> STAFF_ROLES = List.of("HR", "Instructor", "PIREP", "Examination", "Event", "AcademyAdmin", "Schedule");
	
	private final JobPosting _jp;
	
	private boolean _canComment;
	private boolean _canViewApplications;
	private boolean _canApply;
	private boolean _canShortList;
	private boolean _canReset;
	private boolean _canSelect;
	private boolean _canComplete;
	private boolean _canEdit;
	private boolean _canDelete;

	/**
	 * Initializes the Access Controller.
	 * @param ctx the SecurityContext
	 * @param jp the JobPosting bean
	 */
	public JobPostingAccessControl(SecurityContext ctx, JobPosting jp) {
		super(ctx);
		_jp = jp;
	}
	
	/**
	 * Calculates access rights.
	 * @throws AccessControlException if the user cannot view the data 
	 */
	@Override
	public void validate() throws AccessControlException {
		validateContext();

		// Check create access
		boolean isHR = _ctx.isUserInRole("HR");
		_canEdit = isHR;
		if (_jp == null)
			return;
		
		// Check we're staff
		Pilot p = _ctx.getUser();
		boolean isHireMgr = _ctx.isAuthenticated() && (p.getID() == _jp.getHireManagerID());
		boolean isStaff = _ctx.isAuthenticated() && (p.getRank().isCP() || RoleUtils.hasAccess(p.getRoles(), STAFF_ROLES));
		boolean canRead = (_jp.getStatus() == JobPosting.OPEN) || isHireMgr || isHR;
		if (_jp.getStaffOnly())
			canRead &= isStaff;
		
		// Check our access
		if (!canRead)
			throw new AccessControlException("Not Authorized");
		if (!_ctx.isUserInRole("Pilot"))
			return;
		
		// Check if we have applications
		_canEdit = isHR && (_jp.getStatus() < JobPosting.SELECTED);
		_canDelete = (_jp.getAppCount() > 0) ? _ctx.isUserInRole("Admin") : isHR;
		_canViewApplications = isHR || (isHireMgr && (_jp.getStatus() >= JobPosting.SHORTLIST));
		_canShortList = isHR && (_jp.getStatus() == JobPosting.CLOSED);
		_canReset = isHR && (_jp.getStatus() == JobPosting.SHORTLIST);
		_canSelect = (_jp.getStatus() == JobPosting.SHORTLIST) && (isHR || isHireMgr);
		_canComment = isHR || _canShortList || _canSelect;
		_canComplete = isHR && (_jp.getStatus() == JobPosting.SELECTED);
			
		// Check whether we can apply
		Instant now = Instant.now();
		Instant minDate = now.minusSeconds(_jp.getMinAge() * 86400);
		_canApply = (_jp.getStatus() == JobPosting.OPEN) && (_jp.getClosesOn() != null) && (_jp.getClosesOn().isAfter(now)) && !isHireMgr;
		_canApply &= (_jp.getMinLegs() <= p.getLegs()) && (p.getCreatedOn().isBefore(minDate));
		if (_jp.getStaffOnly())
			_canApply &= isStaff;
		
		// Check if we've already applied
		for (Application a : _jp.getApplications())
			_canApply &= (a.getAuthorID() != p.getID());
	}

	/**
	 * Returns whether the current user can apply for this Job.
	 * @return TRUE if the user can apply, otherwise FALSE
	 */
	public boolean getCanApply() {
		return _canApply;
	}
	
	/**
	 * Returns whether the current user can comment on this Job Posting.
	 * @return TRUE if the user can create a comment, otherwise FALSE
	 */
	public boolean getCanComment() {
		return _canComment;
	}
	
	/**
	 * Returns whether the current user can view applications for this Job.
	 * @return TRUE if the user can view applications, otherwise FALSE
	 */
	public boolean getCanViewApplicants() {
		return _canViewApplications;
	}
	
	/**
	 * Returns whether the current user can shortlist applicants for this Job. 
	 * @return TRUE if the user can shortlist applicants, otherwise FALSE
	 */
	public boolean getCanShortlist() {
		return _canShortList;
	}
	
	/**
	 * Returns whether the current user can reset the shortlist process.
	 * @return TRUE if the shortlist can be reset, otherwise FALSE
	 */
	public boolean getCanReset() {
		return _canReset;
	}
	
	/**
	 * Returns whether the current user can select a short-listed applicant for approval.
	 * @return TRUE if the user can select an applicant, otherwise RALSE
	 */
	public boolean getCanSelect() {
		return _canSelect;
	}
	
	/**
	 * Returns whether the current user can edit this Job Posting.
	 * @return TRUE if the user can edit the Job Posting, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}
	
	/**
	 * Returns whether the current user can complete this Job Posting.
	 * @return TRUE if the user can complete the posting, otherwise FALSE
	 */
	public boolean getCanComplete() {
		return _canComplete;
	}
	
	/**
	 * Returns whether the current user can delete this Job Posting.
	 * @return TRUE if the user can delete the Job Posting, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}