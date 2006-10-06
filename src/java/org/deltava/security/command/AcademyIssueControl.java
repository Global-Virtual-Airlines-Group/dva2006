// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.Person;
import org.deltava.beans.academy.Issue;

import org.deltava.commands.CommandException;
import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Flight Academy Issues.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AcademyIssueControl extends AccessControl {
	
	private Issue _i;
	private boolean _hasCourse;
	
	private boolean _canCreate;
	private boolean _canComment;
	private boolean _canUpdateStatus;
	private boolean _canDelete;

	/**
	 * Initializes the Access Controller.
	 * @param ctx the Security Context
	 * @param i the Issue bean
	 * @param hasCourse TRUE if the user has an existing Flight Academy Course, otherwise FALSE
	 */
	public AcademyIssueControl(SecurityContext ctx, Issue i, boolean hasCourse) {
		super(ctx);
		_i = i;
		_hasCourse = hasCourse;
	}

	/**
	 * Calculates access rights.
	 * @throws AccessControlException if the Issue cannot be read
	 */
	public void validate() throws CommandException {
		validateContext();
		
		// Get the person object
		Person p = _ctx.getUser();
		if ((p == null) || (!p.isInRole("Pilot")))
			throw new AccessControlException("Not Authorized");
		
		// Calculate creation rights
		boolean isAdmin = _ctx.isUserInRole("Instructor") || _ctx.isUserInRole("Examiner") || _ctx.isUserInRole("HR");
		if (_i == null) {
			_canCreate = isAdmin || _hasCourse;
			return;
		}

		// Calculate access rights
		boolean isMine = (_i.getAuthorID() == p.getID());
		_canComment = isMine || isAdmin;
		_canUpdateStatus = isAdmin;
		_canDelete = _ctx.isUserInRole("Admin");
	}
	
	/**
	 * Returns wether the user can create a new Flight Academy Issue.
	 * @return TRUE if a new Issue can be created, otherwise FALSE
	 */
	public boolean getCanCreate() {
		return _canCreate;
	}

	/**
	 * Returns wether the user can add a comment to this Issue.
	 * @return TRUE if a Comment can be created, otherwise FALSE
	 */
	public boolean getCanComment() {
		return _canComment;
	}
	
	/**
	 * Returns wether this Issue can have its status changed. 
	 * @return TRUE if the status can be changed, otherwise FALSE
	 */
	public boolean getCanUpdateStatus() {
		return _canUpdateStatus;
	}
	
	/**
	 * Returns wether this Issue can be deleted.
	 * @return TRUE if the Issue can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}