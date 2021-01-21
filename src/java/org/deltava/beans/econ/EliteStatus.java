// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

import java.time.Instant;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to store Pilots' Elite status level data. 
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

public class EliteStatus extends DatabaseBean implements EliteLevelBean {
	
	private EliteLevel _lvl;
	private UpgradeReason _ur = UpgradeReason.NONE;
	private Instant _created;

	/**
	 * Creates the bean.
	 * @param pilotID the Pilot's database ID
	 * @param lvl the EliteLevel
	 */
	public EliteStatus(int pilotID, EliteLevel lvl) {
		setID(pilotID);
		_lvl = lvl;
	}
	
	@Override
	public EliteLevel getLevel() {
		return _lvl;
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
		_lvl = lvl;
	}
	
	/**
	 * Generates the cache key for this type of bean.
	 * @param year the plan year
	 * @param id the pilot's database ID
	 * @return the cache key
	 */
	public static Long generateKey(int year, int id) {
		return Long.valueOf((year << 32) + id);
	}
	
	@Override
	public Object cacheKey() {
		return generateKey(_lvl.getYear(), getID());
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(getID());
		buf.append('/').append(_lvl.toString());
		buf.append('/').append(_ur.name());
		return buf.toString();
	}
	
	@Override
	public int compareTo(Object o2) {
		int tmpResult = super.compareTo(o2);
		if ((tmpResult == 0) && (o2 instanceof EliteStatus)) {
			EliteStatus es2 = (EliteStatus) o2;
			tmpResult = Integer.compare(_lvl.getYear(), es2.getLevel().getYear());
			if (tmpResult == 0)
				tmpResult = _lvl.compareTo(es2._lvl);
		}
		
		return tmpResult;
	}
}