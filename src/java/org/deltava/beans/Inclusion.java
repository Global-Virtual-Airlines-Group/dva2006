// Copyright 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An enumeration to store inclusion/exclusion values.
 * @author Luke
 * @version 9.0
 * @since 8.6
 */

public enum Inclusion implements ComboAlias, EnumDescription {
	ALL, INCLUDE, EXCLUDE;

	@Override
	public String getComboAlias() {
		return name();
	}

	@Override
	public String getComboName() {
		return getDescription();
	}
}