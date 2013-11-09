// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface for unit enumerations.
 * @author Luke
 * @version 5.2
 * @since 5.2
 */

public interface Unit extends ComboAlias {

	/**
	 * Returns the unit name.
	 * @return the full name
	 */
	public String getUnitName();
	
	/**
	 * Returns the ratio of this distance unit to a reference unit.
	 * @return the ratio
	 */
	public double getFactor();
	
	/**
	 * Returns the enumeration name.
	 * @return the name
	 */
	public String name();
}