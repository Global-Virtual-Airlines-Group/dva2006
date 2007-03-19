// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;

import org.deltava.beans.Person;
import org.deltava.beans.Applicant;

/**
 * An Access Controller to support Applicant profile operations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public final class ApplicantAccessControl extends AccessControl {

	private Applicant _ap;
	
	private boolean _canRead;
	private boolean _canEdit;
	private boolean _canApprove;
	private boolean _canReject;
	private boolean _canNotify;
	private boolean _canDelete;
	
	/**
	 * Initializes the controller.
	 * @param ctx the Command context
	 * @param a the Applicant profile
	 */
	public ApplicantAccessControl(SecurityContext ctx, Applicant a) {
		super(ctx);
		_ap = a;
	}

	/**
	 * Calculates access rights.
	 * @throws AccessControlException if the user cannot even read the profile
	 */
	public void validate() throws AccessControlException {
		validateContext();
		
		// Gets the person object
		Person p = _ctx.getUser();
		if (p == null)
			throw new AccessControlException("Not Authorized");
		
		// Sets role variables
		boolean isOurs = (_ap.getID() == p.getID());
		boolean isHR = p.isInRole("HR");
		boolean isPending = (_ap.getStatus() == Applicant.PENDING);
		
		// Set state variables
		_canRead = (isOurs || isHR);
		_canEdit = (isHR && isPending);
		_canApprove = _canEdit;
		_canReject = _canEdit;
		_canNotify = isHR && (_ap.getStatus() != Applicant.REJECTED);
		_canDelete = (isHR && isPending) || (_ctx.isUserInRole("Admin") && (_ap.getStatus() == Applicant.REJECTED));  
	}

   /**
    * Returns if the Applicant profile can be read.
    * @return TRUE if the profile can be read, otherwise FALSE
    */
	public boolean getCanRead() {
		return _canRead;
	}
	
	/**
    * Returns if the Applicant profile can be edited.
    * @return TRUE if the profile can be edited, otherwise FALSE
    */
	public boolean getCanEdit() {
		return _canEdit;
	}
	
	/**
    * Returns if the Applicant can be approved.
    * @return TRUE if the Applicant can be approved, otherwise FALSE
    */
	public boolean getCanApprove() {
		return _canApprove;
	}
	
	/**
    * Returns if the Applicant can be rejected.
    * @return TRUE if the Applicant can be rejected, otherwise FALSE
    */
	public boolean getCanReject() {
		return _canReject;
	}
	
	/**
	 * Returns if the user can send a notification message to the Applicant.
	 * @return TRUE if the Applicant can be notified, otherwise FALSE
	 */
	public boolean getCanNotify() {
		return _canNotify;
	}
	
	/**
	 * Returns if the user can delete the Applicant.
	 * @return TRUE if the Applicant can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}