// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

/**
 * An enumeration for ILS categories. 
 * @author Luke
 * @version 4.1
 * @since 4.1
 */

public enum ILSCategory {
	NONE(2500, 3500), CATI(200, 2400), CATII(100, 1150), CATIIIa(50, 660), CATIIIb(25, 250), CATIIIc(10, 50);

	private int _ceiling;
	private int _viz;
	
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
	
	/**
	 * Returns an ILS category based on name.
	 * @param name the category name
	 * @return an ILSCategory, or null if not found
	 */
	public static ILSCategory get(String name) {
		for (ILSCategory ic : values()) {
			if (ic.name().equalsIgnoreCase(name))
				return ic;
        }
		
		return ILSCategory.NONE;
	}
}