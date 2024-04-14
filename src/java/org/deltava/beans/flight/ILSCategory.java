// Copyright 2011, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

/**
 * An enumeration for ILS categories. 
 * @author Luke
 * @version 11.2
 * @since 4.1
 */

public enum ILSCategory {
	NONE(1500, 3500), CATI(200, 2400), CATII(100, 1150), CATIIIa(50, 660), CATIIIb(25, 250), CATIIIc(10, 50);

	private final int _ceiling;
	private final int _viz;
	
	ILSCategory(int ceiling, int viz) {
		_ceiling = ceiling;
		_viz = viz;
	}

	/**
	 * Returns the minimum cloud ceiling for this category.
	 * @return the ceiling in feet AGL
	 */
	public int getCeiling() {
		return _ceiling;
	}
	
	/**
	 * Returns the minimum runway visibility for this category.
	 * @return the visibility in feet
	 */
	public int getVisibility() {
		return _viz;
	}
	
	/**
	 * Returns the ILS category for a given set of conditions.
	 * @param ceiling the cloud ceiling in feet
	 * @param viz the runway visibility in feet
	 * @return an ILSCategory
	 */
	public static ILSCategory categorize(int ceiling, double viz) {
        for (ILSCategory ic : values()) {
            if ((viz > ic.getVisibility()) && (ceiling > ic.getCeiling()))
                return ic;
        }
        
        return CATIIIc;
	}
}