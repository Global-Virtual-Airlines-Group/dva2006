// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An enumeration to store inclusion/exclusion values.
 * @author Luke
 * @version 8.6
 * @since 8.6
 */

public enum Inclusion implements ComboAlias {
	ALL, INCLUDE, EXCLUDE;

	@Override
	public String getComboAlias() {
		return name();
	}

	@Override
	public String getComboName() {
		return name().substring(0, 1).concat(name().substring(1).toLowerCase());
	}
	
	/**
	 * Exception-safe parser.
	 * @param v the value
	 * @return an Inclusion
	 */
	public static Inclusion parse(String v) {
		try {
			return Inclusion.valueOf(v.toUpperCase());
		} catch (Exception e) {
			return Inclusion.ALL;
		}
	}
}