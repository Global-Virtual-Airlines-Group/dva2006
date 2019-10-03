// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import org.deltava.beans.ComboAlias;

/**
 * An enumeration of Check Ride PIREP approval options.
 * @author Luke
 * @version 8.7
 * @since 8.7
 * @see CheckRideScoreOptions
 */

public enum CheckFlightScoreOptions implements ComboAlias {
	APPROVE("Approve Flight", "true"), REJECT("Reject Flight", "false");
	
	private final String _label;
	private final String _value;
	
	CheckFlightScoreOptions(String label, String value) {
		_label = label;
		_value = value;
	}

	@Override
	public String getComboAlias() {
		return _value;
	}

	@Override
	public String getComboName() {
		return _label;
	}
}