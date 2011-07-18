// Copyright 2006, 2008, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * An interface to store Online Network constants.
 * @author Luke
 * @version 4.0
 * @since 1.0
 */

public enum OnlineNetwork {
	
	VATSIM(0), IVAO(1), FPI(2), INTVAS(3), ACARS(4);

	private final int _value;
	
	OnlineNetwork(int value) {
		_value = value;
	}
	
	public int getValue() {
		return _value;
	}
}