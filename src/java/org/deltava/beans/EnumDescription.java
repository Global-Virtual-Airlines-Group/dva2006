// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface for Enumerations with propercased descriptions. 
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public interface EnumDescription extends ComboAlias {

	/**
	 * The enumeration name.
	 * @return the name
	 */
	abstract String name();
	
	/**
	 * The propercased description.
	 * @return the description
	 */
	default String getDescription() {
		return name().substring(0, 1) + name().substring(1).toLowerCase();
	}
	
	@Override
	default String getComboAlias() {
		return name();
	}
	
	@Override
	default String getComboName() {
		return getDescription();
	}
}