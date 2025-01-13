// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

import java.time.Instant;

import org.deltava.beans.*;

/**
 * A bean to track Pilots achieving a lifetime Elite status. 
 * @author Luke
 * @version 11.5
 * @since 11.5
 */

public class EliteLifetimeStatus extends DatabaseBean implements EliteLevelBean {
	
	private final EliteLifetime _el;
	private UpgradeReason _ur = UpgradeReason.NONE;
	private Instant _created;

	/**
	 * Helper class to support lifetime Elite status.
	 */
	public class LifetimeStatus extends EliteStatus {

		LifetimeStatus(int pilotID, EliteLifetime el) {
			super(pilotID, el.getLevel());
		}

		@Override
		public boolean getIsLifetime() {
			return true;
		}
	}

	/**
	 * Creates the bean.
	 * @param pilotID the Pilot's database ID
	 * @param el the EliteLifetime status achieved
	 */
	public EliteLifetimeStatus(int pilotID, EliteLifetime el) {
		super();
		setID(pilotID);
		_el = el;
	}
	
	/**
	 * Returns the lifetime Elite status achieved.
	 * @return an EliteLifetime bean
	 */
	public EliteLifetime getLifetimeStatus() {
		return _el;
	}
	
	/**
	 * Creates an EliteStatus bean with the equivalent status level.
	 * @return an EliteStatus bean
	 */
	public EliteStatus toStatus() {
		EliteStatus es = new LifetimeStatus(getID(), _el);
		es.setEffectiveOn(_created);
		es.setUpgradeReason(_ur);
		return es;
	}
	
	@Override
	public EliteLevel getLevel() {
		return _el.getLevel();
	}
	
	@Override
	public int getColor() {
		return _el.getLevel().getColor();
	}

	/**
	 * Returns the effective date of this status.
	 * @return the effective date
	 */
	public Instant getEffectiveOn() {
		return _created;
	}
	
	/**
	 * Returns the reason for this status upgrade.
	 * @return an UpgradeReason
	 */
	public UpgradeReason getUpgradeReason() {
		return _ur;
	}
	
	/**
	 * Returns the effective date of this status.
	 * @param dt the effective date
	 */
	public void setEffectiveOn(Instant dt) {
		_created = dt;
	}
	
	/**
	 * Updates the reason for this status upgrade.
	 * @param ur an UpgradeReason
	 */
	public void setUpgradeReason(UpgradeReason ur) {
		_ur = ur;
	}
	
	@Override
	public void setLevel(EliteLevel lvl) {
		_el.setLevel(lvl);
	}
}