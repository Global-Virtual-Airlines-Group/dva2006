// Copyright 2012, 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

/**
 * An enumeration to store serialized ACARS position data version information.
 * @author Luke
 * @version 5.4
 * @since 4.1
 */

public enum SerializedDataVersion {
	ACARS(1, false), XACARS(1, true), ACARSv2(2, false);

	private int _version;
	private boolean _isXACARS;
	
	SerializedDataVersion(int version, boolean isXACARS) {
		_version = version;
		_isXACARS = isXACARS;
	}
	
	public int getVersion() {
		return _version;
	}
	
	public boolean isXACARS() {
		return _isXACARS;
	}
}