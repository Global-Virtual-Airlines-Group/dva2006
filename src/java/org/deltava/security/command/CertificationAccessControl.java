// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;

/**
 * An Access Controller for Flight Academy certification profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CertificationAccessControl extends AccessControl {
	
	private boolean _canCreate;
	private boolean _canEdit;
	private boolean _canDelete;
	
	private boolean _canCreateVideo;
	private boolean _canEditVideo;

	/**
	 * Creates the Access Controller.
	 * @param ctx the security context
	 */
	public CertificationAccessControl(SecurityContext ctx) {
		super(ctx);
	}

	/**
	 * Calculates access rights.
	 * @throws AccessControlException if the user cannot view the Certification profile.
	 */
	public void validate() throws AccessControlException {
		validateContext();

		// Calculate roles - abort if we cannot read
		boolean isHR = _ctx.isUserInRole("HR");
		boolean isAcademyAdmin = _ctx.isUserInRole("AcademyAdmin");
		if (!isHR && !_ctx.isUserInRole("Instructor") && !isAcademyAdmin)
			throw new AccessControlException("Not Authorized");
		
		_canCreate = isHR || isAcademyAdmin;
		_canCreateVideo = isHR || isAcademyAdmin;
		_canEdit = isHR || isAcademyAdmin;
		_canEditVideo = isHR || isAcademyAdmin;
		_canDelete = _ctx.isUserInRole("Admin");
	}
	
	/**
	 * Returns if the user can create a new Certification profile.
	 * @return TRUE if a profile can be created, otherwise FALSE
	 */
	public boolean getCanCreate() {
		return _canCreate;
	}

	/**
	 * Returns if the user can edit a Certification profile.
	 * @return TRUE if the profile can be edited, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}
	
	/**
	 * Returns if the user can delete a Certification profile or training video.
	 * @return TRUE if the profile can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
	
	/**
	 * Returns if the user can create a Flight Academy training video.
	 * @return TRUE if a video can be created, otherwise FALSE
	 */
	public boolean getCanCreateVideo() {
		return _canCreateVideo;
	}
	
	/**
	 * Returns if the user can edit a Flight Academy training video.
	 * @return TRUE if a video can be edited, otherwise FALSE
	 */
	public boolean getCanEditVideo() {
		return _canEditVideo;
	}
}