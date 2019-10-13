// Copyright 2012, 2014, 2016, 2017, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

/**
 * An enumeration to store serialized ACARS position data version information.
 * @author Luke
 * @version 8.7
 * @since 4.1
 */

public enum SerializedDataVersion {
	ACARS(1, false), XACARS(1, true), ACARSv2(2, false), ACARSv3(3, false), ACARSv4(4, false), ACARSv41(4, false), ACARSv5(5, false), ACARSv6(6, false), ACARSv7(7, false);

	private final int _version;
	private final boolean _isXACARS;
	
	SerializedDataVersion(int version, boolean isXACARS) {
		_version = version;
		_isXACARS = isXACARS;
	}

	/**
	 * Returns the version number.
	 * @return the version
	 */
	public int getVersion() {
		return _version;
	}
	
	/**
	 * Returns whether this is an XACARS-generated data stream.
	 * @return TRUE if created by XACARS, otherwise FALSE
	 */
	public boolean isXACARS() {
		return _isXACARS;
	}
	
	/**
	 * Exception-safe lookup method.
	 * @param code the ordinal code, or -1 for unknown
	 * @return a SerializedDataVersion, or null
	 */
	public static SerializedDataVersion fromCode(int code) {
		if ((code < 0) || (code >= values().length)) return null;
		return values()[code];
	}
}