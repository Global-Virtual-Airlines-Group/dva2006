// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.gallery.Image;

import org.deltava.security.SecurityContext;
import org.deltava.commands.CommandSecurityException;

/**
 * An Access Controller to support Image Gallery operations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class GalleryAccessControl extends AccessControl {

	private Image _img;
	
	private boolean _canCreate;
	private boolean _canEdit;
	private boolean _canVote;
	private boolean _canDelete;
	
	/**
	 * Initializes the access controller.
	 * @param ctx the command context
	 * @param img the Image Gallery image
	 */
	public GalleryAccessControl(SecurityContext ctx, Image img) {
		super(ctx);
		_img = img;
	}

	 /**
     * Calculates access rights.
     * @throws CommandSecurityException never
     */
	public void validate() throws CommandSecurityException {
	   validateContext();
		
		//	If no image specified only calculate creation rights
	   _canCreate = _ctx.isUserInRole("Pilot");
		if (_img == null) {
			_canEdit = _canCreate;
			return;
		}

		// Calculate access rights
		_canEdit = _ctx.isUserInRole("Gallery");
		_canDelete = _ctx.isUserInRole("Admin");
		_canVote = _ctx.isAuthenticated() && _canCreate && !_img.hasVoted(_ctx.getUser());
	}
	
	/**
	 * Returns if a new Image Gallery entry can be created.
	 * @return TRUE if a new entry can be created, otherwise FALSE
	 */
	public boolean getCanCreate() {
		return _canCreate;
	}
	
	/**
	 * Returns if the Image Gallery entry can be edited.
	 * @return TRUE if the entry can be edited, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}
	
	/**
	 * Returns if this user can vote for this Image Gallery entry.
	 * @return TRUE if a vote can be cast, otherwise FALSE
	 */
	public boolean getCanVote() {
		return _canVote;
	}
	
	/**
	 * Returns if the Image Gallery entry can be deleted.
	 * @return TRUE if the entry can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}