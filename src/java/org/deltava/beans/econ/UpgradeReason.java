// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

/**
 * An enumeration to store final Elite status upgrade criteria. If an Elite status requires multiple criteria
 * for an upgrade, this records the last criteria to be fulfilled.
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

public enum UpgradeReason implements org.deltava.beans.EnumDescription {
	NONE, LEGS, DISTANCE, POINTS, ROLLOVER, DOWNGRADE;
	
	/**
	 * Returns if this reason is a rollover of the previous year's status.
	 * @return TRUE if a Rollover or Downgrade, otherwise FALSE
	 */
	public boolean isRollover() {
		return ((this == ROLLOVER) || (this == DOWNGRADE));
	}
}