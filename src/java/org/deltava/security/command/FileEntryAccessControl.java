// Copyright 2005, 2006, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.fleet.FileEntry;
import org.deltava.security.SecurityContext;

/**
 * An Access Controller for File Library entires.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class FileEntryAccessControl extends FleetEntryAccessControl {

	/**
	 * Initializes the Access Controller.
	 * @param ctx the Security context
	 * @param e the File Library entry
	 */
	public FileEntryAccessControl(SecurityContext ctx, FileEntry e) {
		super(ctx, e);
	}

	/**
	 * Returns if a new File Library entry can be created.
	 * @return TRUE if a new entry can be created, otherwise FALSE
	 */
	@Override
	public boolean getCanCreate() {
		return super.getCanCreate() || _ctx.isUserInRole("HR");
	}

	/**
	 * Returns if the File Library entry can be edited.
	 * @return TRUE if the entry can be edited, otherwise FALSE
	 */
	@Override
	public boolean getCanEdit() {
		return super.getCanEdit() || _ctx.isUserInRole("HR");
	}
}