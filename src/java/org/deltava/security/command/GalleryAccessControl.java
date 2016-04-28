// Copyright 2005, 2006, 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.gallery.Image;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller to support Image Gallery operations.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class GalleryAccessControl extends AccessControl {

	private final Image _img;
	
	private boolean _canCreate;
	private boolean _canEdit;
	private boolean _canLike;
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
     */
	@Override
	public void validate() {
	   validateContext();
		
		//	If no image specified only calculate creation rights
	   _canCreate = _ctx.isUserInRole("Pilot");
		if (_img == null) {
			_canEdit = _canCreate;
			return;
		}
		
		// Calculate access rights
		boolean isOurs = _ctx.isAuthenticated() && (_img.getAuthorID() == _ctx.getUser().getID());
		_canEdit = _ctx.isUserInRole("HR") || (isOurs && (_img.getLikeCount() == 0));
		_canDelete = _canEdit;
		_canLike = _ctx.isAuthenticated() && _canCreate && !_img.hasLiked(_ctx.getUser());
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
	 * Returns if this user can like this Image Gallery entry.
	 * @return TRUE if a like can be cast, otherwise FALSE
	 */
	public boolean getCanLike() {
		return _canLike;
	}
	
	/**
	 * Returns if the Image Gallery entry can be deleted.
	 * @return TRUE if the entry can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}