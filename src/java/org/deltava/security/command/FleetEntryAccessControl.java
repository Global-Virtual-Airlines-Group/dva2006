// Copyright 2005, 2006, 2007, 2010, 2014, 2016, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.security.SecurityContext;

import org.deltava.beans.fleet.LibraryEntry;

/**
 * An Access Controller to support Fleet/File/Document Library entry operations.
 * @author Luke
 * @version 10.2
 * @since 1.0
 */

public class FleetEntryAccessControl extends AccessControl {

	protected LibraryEntry _entry;

	protected boolean _canView;
	protected boolean _canEdit;
	protected boolean _canCreate;
	private boolean _canDelete;

	/**
	 * Initializes the access controller.
	 * @param ctx the Command context
	 * @param e the LibraryEntry
	 */
	public FleetEntryAccessControl(SecurityContext ctx, LibraryEntry e) {
		super(ctx);
		_entry = e;
	}

	/**
	 * Updates the Fleet Entry to validate access to.
	 * @param e the entry
	 */
	public void setEntry(LibraryEntry e) {
		_entry = e;
	}

	@Override
	public void validate() {
		validateContext();
		_canCreate = _ctx.isUserInRole("Fleet");
		if (_entry == null) return;

		// Set access variables
		boolean isOurs = _ctx.isAuthenticated() && _ctx.getUser().getAirlineCode().equals(_entry.getOwner().getCode());
		_canEdit = isOurs && _ctx.isUserInRole("Fleet");
		_canDelete = isOurs && _ctx.isUserInRole("HR");
		switch (_entry.getSecurity()) {
		case PUBLIC:
			_canView = true;
			break;

		case AUTH:
			_canView = isOurs;
			break;

		default:
		case STAFF:
			_canView = isOurs && (_ctx.getRoles().size() > 1);
			break;
		}
	}

	/**
	 * Returns if the Fleet/Document Library entry can be edited.
	 * @return TRUE if the entry can be edited, otherwise FALSE
	 */
	public boolean getCanEdit() {
		return _canEdit;
	}

	/**
	 * Returns if the Fleet/Document Library entry can be deleted.
	 * @return TRUE if the entry can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}

	/**
	 * Returns if the Fleet/Document Library entry can be viewed.
	 * @return TRUE if the entry can be viewed, otherwise FALSE
	 */
	public boolean getCanView() {
		return _canView;
	}

	/**
	 * Returns if a new Fleet/Document Library entry can be created.
	 * @return TRUE if a new entry can be created, otherwise FALSE
	 */
	public boolean getCanCreate() {
		return _canCreate;
	}
}