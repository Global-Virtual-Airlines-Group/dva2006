// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.Pilot;
import org.deltava.beans.hr.*;

import org.deltava.security.SecurityContext;

import org.deltava.util.system.SystemData;

/**
 * An access controller for Senior Captain nominations. 
 * @author Luke
 * @version 3.3
 * @since 3.3
 */

public class NominationAccessControl extends AccessControl {

	private Nomination _n;
	
	private boolean _canNominate;
	private boolean _canUpdate;
	private boolean _canDelete;
	
	/**
	 * Intiailizes the Access Controller.
	 * @param n the Nomination bean
	 * @param ctx the SecurityContext
	 */
	public NominationAccessControl(SecurityContext ctx, Nomination n) {
		super(ctx);
		_n = n;
	}

    /**
     * Calculates access rights.
     * @throws AccessControlException if we cannot view the data
     */
	@Override
	public void validate() throws AccessControlException {
		
		Pilot p = (Pilot) _ctx.getUser();
		int daysActive = (int) ((System.currentTimeMillis() - p.getCreatedOn().getTime()) / 86400 / 1000);
		_canNominate = (p.getLegs() > SystemData.getInt("users.sc.minFlights", 5)) && 
			(daysActive > SystemData.getInt("users.sc.minAge", 120));
		if (_n == null)
			return;
		
		// Check if we've nominated someone already
		for (NominationComment nc : _n.getComments())
			_canNominate &= (nc.getAuthorID() != p.getID());
		
		// Check other access
		_canUpdate = _ctx.isUserInRole("HR");
		_canDelete = _ctx.isUserInRole("Admin");
	}
	
	/**
	 * Returns if the user can nominate someone.
	 * @return TRUE if someone can be nominated, otherwise FALSE
	 */
	public boolean getCanNominate() {
		return _canNominate;
	}
	
	/**
	 * Returns if the user can update this Nomination.
	 * @return TRUE if the nomination can be updated, otherwise FALSE
	 */
	public boolean getCanUpdate() {
		return _canUpdate;
	}
	
	/**
	 * Returns if the user can delete this Nomination.
	 * @return TRUE if the nomination can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}