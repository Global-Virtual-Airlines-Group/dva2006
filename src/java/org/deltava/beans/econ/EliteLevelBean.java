// Copyright 2020, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.econ;

import org.deltava.beans.RGBColor;

/**
 * An interface to mark beans that contain an Elite level.
 * @author Luke
 * @version 11.5
 * @since 9.2
 */

public interface EliteLevelBean extends RGBColor {

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
	
	/**
	 * Determines whether this bean's Elite status level is exceeded by another EliteLeveBean.
	 * @param el the EliteLevelBean
	 * @return if the status level is less than that of the other bean
	 */
	default boolean overridenBy(EliteLevelBean el) {
		return (el != null) && (getLevel().compareTo(el.getLevel()) < 0);
	}
}