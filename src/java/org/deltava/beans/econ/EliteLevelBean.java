// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

/**
 * An interface to mark beans that contain an Elite level.
 * @author Luke
 * @version 9.2
 * @since 9.2
 */

public interface EliteLevelBean {

	/**
	 * Returns the Elite status level.
	 * @return the EliteLevel
	 */
	public EliteLevel getLevel();
	
	/**
	 * Updates the Elite status level.
	 * @param lvl the EliteLevel
	 */
	public void setLevel(EliteLevel lvl);
}