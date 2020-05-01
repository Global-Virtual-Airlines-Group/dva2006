// Copyright 2012, 2014, 2016, 2017, 2018, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

/**
 * An enumeration to store serialized ACARS position data version information.
 * @author Luke
 * @version 9.0
 * @since 4.1
 */

public enum SerializedDataVersion {
	ACARS(1), XACARS(1), ACARSv2(2), ACARSv3(3), ACARSv4(4), ACARSv41(4), ACARSv5(5), ACARSv6(6), ACARSv7(7);

	private final int _version;
	
	SerializedDataVersion(int version) {
		_version = version;
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
		return (this == XACARS);
	}
	
	/**
	 * Exception-safe lookup method.
	 * @param code the ordinal code, or -1 for unknown
	 * @return a SerializedDataVersion, or null
	 */
	public static SerializedDataVersion fromCode(int code) {
		return ((code < 0) || (code >= values().length)) ? null : values()[code];
	}
}