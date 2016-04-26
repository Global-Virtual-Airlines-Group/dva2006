// Copyright 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security.command;

import org.deltava.beans.Pilot;
import org.deltava.beans.Rank;
import org.deltava.beans.hr.*;

import org.deltava.security.SecurityContext;

import org.deltava.util.system.SystemData;

/**
 * An access controller for Senior Captain nominations. 
 * @author Luke
 * @version 7.0
 * @since 3.3
 */

public class NominationAccessControl extends AccessControl {

	private final Nomination _n;
	private Pilot _p;
	
	private boolean _canRead;
	private boolean _canNominate;
	private boolean _canNominateUnlimited;
	private boolean _canObject;
	private boolean _canUpdate;
	private boolean _canDispose;
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
	 * Sets the Pilot being nominated.
	 * @param p the Pilot being nominated
	 */
	public void setPilot(Pilot p) {
		_p = p;
	}

    /**
     * Calculates access rights.
     * @throws AccessControlException if we cannot view the data
     */
	@Override
	public void validate() throws AccessControlException {
		
		// Check staff
		boolean isStaff = _ctx.isUserInRole("HR") || _ctx.isUserInRole("Operations") || _ctx.isUserInRole("PIREP") ||
			_ctx.isUserInRole("Event") || _ctx.isUserInRole("Instructor") || _ctx.isUserInRole("AcademyAdmin");
		
		Pilot usr = (Pilot) _ctx.getUser();
		int daysActive = (int) ((System.currentTimeMillis() - usr.getCreatedOn().toEpochMilli()) / 86400_000);
		_canNominate = isStaff || (usr.getLegs() > SystemData.getInt("users.sc.minFlights", 5)) && 
			(daysActive > SystemData.getInt("users.sc.minAge", 120));
		_canNominateUnlimited = usr.getRank().isCP() || isStaff;
		if (_n == null)
			return;
		
		// Check if the Pilot isn't already a Senior Captain
		if (_p != null)
			_canNominate &= (_p.getRank() != Rank.SC) && (!_p.getRank().isCP());
		
		// Check if we've nominated someone already
		for (NominationComment nc : _n.getComments())
			_canNominate &= (nc.getAuthorID() != usr.getID());
		
		// Check other access
		_canObject = _canNominate && _canNominateUnlimited;
		_canUpdate = _ctx.isUserInRole("HR");
		_canDispose = _canUpdate && (_n.getStatus() == Nomination.Status.PENDING);
		_canDelete = _ctx.isUserInRole("Admin");
		_canRead = _canObject || _canUpdate;
	}
	
	/**
	 * Returns if the user can nominate someone.
	 * @return TRUE if someone can be nominated, otherwise FALSE
	 */
	public boolean getCanNominate() {
		return _canNominate;
	}
	
	/**
	 * Returns if the user can make an unlimited number of Nominations. 
	 * @return TRUE if nominations unlimited, otherwise FALSE
	 */
	public boolean getCanNominateUnlimited() {
		return _canNominateUnlimited;
	}
	
	/**
	 * Returns if the user can read this nomination.
	 * @return TRUE if the nomination can be read, otherwise FALSE
	 */
	public boolean getCanRead() {
		return _canRead;
	}
	
	/**
	 * Returns if the user can provide negative comments to the nomination.
	 * @return TRUE if the user can object, otherwise FALSE
	 */
	public boolean getCanObject() {
		return _canObject;
	}
	
	/**
	 * Returns if the user can update this Nomination.
	 * @return TRUE if the nomination can be updated, otherwise FALSE
	 */
	public boolean getCanUpdate() {
		return _canUpdate;
	}
	
	/**
	 * Returns if the user can approve or reject this Nomination.
	 * @return TRUE if the nomation can be rejected or approved, otherwise FALSE
	 */
	public boolean getCanDispose() {
		return _canDispose;
	}
	
	/**
	 * Returns if the user can delete this Nomination.
	 * @return TRUE if the nomination can be deleted, otherwise FALSE
	 */
	public boolean getCanDelete() {
		return _canDelete;
	}
}