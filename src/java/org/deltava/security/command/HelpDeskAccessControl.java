// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.Person;
import org.deltava.beans.help.Issue;

import org.deltava.commands.CommandException;
import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Help Desk Issues.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class HelpDeskAccessControl extends AccessControl {
	
	private Issue _i;
	
	private boolean _canCreate;
	private boolean _canClose;
	private boolean _canComment;
	private boolean _canUpdateStatus;
	private boolean _canUpdateContent;

	/**
	 * Initializes the Access Controller.
	 * @param ctx the Security Context
	 * @param i the Issue bean
	 */
	public HelpDeskAccessControl(SecurityContext ctx, Issue i) {
		super(ctx);
		_i = i;
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
		_canCreate = true;
		if (_i == null)
			return;

		// Calculate access rights
		boolean isHR = _ctx.isUserInRole("HR");
		boolean isHelpDesk = _ctx.isUserInRole("HelpDesk");
		boolean isAcademy = _ctx.isUserInRole("Instructor") || _ctx.isUserInRole("AcademyAdmin");
		boolean isAdmin = isHR || isAcademy || _ctx.isUserInRole("PIREP") || _ctx.isUserInRole("Examination") || _ctx.isUserInRole("Signature");
		boolean isMine = (_i.getAuthorID() == p.getID());
		boolean isOpen = (_i.getStatus() == Issue.OPEN);
		if (!_i.getPublic() && !isMine && !isAdmin && !isHelpDesk)
			throw new AccessControlException("Not Authorized");
		
		_canComment = (isMine && isOpen) || (_i.getPublic() && isOpen) || isHelpDesk || isAdmin;
		_canUpdateStatus = isAdmin || isHelpDesk;
		_canClose = _canUpdateStatus && (_i.getStatus() != Issue.CLOSED);
		_canUpdateContent = isHR;
	}
	
	/**
	 * Returns wether the user can create a new Help Desk Issue.
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
	 * Returns wether this Issue can have its status changed to Closed. 
	 * @return TRUE if the status can be changed, otherwise FALSE
	 */
	public boolean getCanClose() {
		return _canClose;
	}
	
	/**
	 * Returns wether this Issue can have its FAQ status or Comments changed.
	 * @return TRUE if the content can be modified, otherwise FALSE
	 */
	public boolean getCanUpdateContent() {
		return _canUpdateContent;
	}
}