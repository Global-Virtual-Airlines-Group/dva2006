// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

/**
 * An enumeration to store IP address types.
 * @author Luke
 * @version 5.2
 * @since 5.2
 */

public enum IPAddress {
	IPV4(32), IPV6(128);
	
	private final int _bits;
	
	IPAddress(int bits) {
		_bits = bits;
	}
	
	public int getBits() {
		return _bits;
	}
}