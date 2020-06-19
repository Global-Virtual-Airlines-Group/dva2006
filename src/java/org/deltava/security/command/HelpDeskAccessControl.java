// Copyright 2006, 2007, 2008, 2010, 2012, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.Person;
import org.deltava.beans.help.*;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Help Desk Issues.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class HelpDeskAccessControl extends AccessControl {
	
	private final Issue _i;
	
	private boolean _canCreate;
	private boolean _canClose;
	private boolean _canComment;
	private boolean _canUpdateStatus;
	private boolean _canUpdateContent;
	
	private boolean _canUpdateTemplate;
	private boolean _canUseTemplate;

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
	@Override
	public void validate() throws AccessControlException {
		validateContext();
		
		// Get the person object
		Person p = _ctx.getUser();
		if ((p == null) || (!p.isInRole("Pilot")))
			throw new AccessControlException("Not Authorized");
		
		// Load roles
		boolean isHR = _ctx.isUserInRole("HR");
		boolean isHelpDesk = _ctx.isUserInRole("HelpDesk");
		boolean isAcademy = _ctx.isUserInRole("Instructor") || _ctx.isUserInRole("AcademyAdmin");
		boolean isAdmin = isHR || isAcademy || _ctx.isUserInRole("PIREP") || _ctx.isUserInRole("Examination") || _ctx.isUserInRole("Signature") || _ctx.isUserInRole("Operations");
		
		// Calculate template rights
		_canUpdateTemplate = isHR;
		_canUseTemplate = isHR || isHelpDesk || isAcademy || isAdmin;
		
		// Calculate creation rights
		_canCreate = true;
		if (_i == null) return;

		// Calculate access rights
		boolean isMine = (_i.getAuthorID() == p.getID());
		boolean isOpen = (_i.getStatus() == IssueStatus.OPEN);
		if (!_i.getPublic() && !isMine && !isAdmin && !isHelpDesk)
			throw new AccessControlException("Not Authorized");
		
		_canComment = isMine || (_i.getPublic() && isOpen) || isHelpDesk || isAdmin;
		_canUpdateStatus = isAdmin || isHelpDesk;
		_canClose = _canUpdateStatus && (_i.getStatus() != IssueStatus.CLOSED);
		_canUpdateContent = isHR;
		
	}
	
	/**
	 * Returns whether the user can create a new Help Desk Issue.
	 * @return TRUE if a new Issue can be created, otherwise FALSE
	 */
	public boolean getCanCreate() {
		return _canCreate;
	}

	/**
	 * Returns whether the user can add a comment to this Issue.
	 * @return TRUE if a Comment can be created, otherwise FALSE
	 */
	public boolean getCanComment() {
		return _canComment;
	}
	
	/**
	 * Returns whether this Issue can have its status changed. 
	 * @return TRUE if the status can be changed, otherwise FALSE
	 */
	public boolean getCanUpdateStatus() {
		return _canUpdateStatus;
	}
	
	/**
	 * Returns whether this Issue can have its status changed to Closed. 
	 * @return TRUE if the status can be changed, otherwise FALSE
	 */
	public boolean getCanClose() {
		return _canClose;
	}
	
	/**
	 * Returns whether this Issue can have its FAQ status or Comments changed.
	 * @return TRUE if the content can be modified, otherwise FALSE
	 */
	public boolean getCanUpdateContent() {
		return _canUpdateContent;
	}

	/**
	 * Returns whether the user can modify a Help Desk Response Template.
	 * @return TRUE if a template can be modified, otherwise FALSE
	 */
	public boolean getCanUpdateTemplate() {
		return _canUpdateTemplate;
	}

	/**
	 * Returns whether the user can use a Help Desk Response Template.
	 * @return TRUE if a template can be used, otherwise FALSE
	 */
	public boolean getCanUseTemplate() {
		return _canUseTemplate;
	}
}