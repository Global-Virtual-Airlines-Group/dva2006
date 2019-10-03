// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import org.deltava.beans.ComboAlias;

/**
 * An enumeration of check ride scoring options. 
 * @author Luke
 * @version 8.7
 * @since 8.7
 * @see CheckFlightScoreOptions
 */

public enum CheckRideScoreOptions implements ComboAlias {
	NONE("Do Not Score"), PASS("Passed"), FAIL("Unsatisfactory");
	
	private final String _label;
	
	CheckRideScoreOptions(String label) {
		_label = label;
	}

	@Override
	public String getComboAlias() {
		return String.valueOf(ordinal());
	}

	@Override
	public String getComboName() {
		return _label;
	}
}