// Copyright 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

/**
 * An enumeration of browsing device types.
 * @author Luke
 * @version 5.2
 * @since 5.2
 */

public enum DeviceType {

	DESKTOP, TABLET, PHONE, UNKNOWN;
	
	/**
	 * Detects a device type based on a user agent header.
	 * @param userAgent the user agent header value
	 * @return a DeviceType
	 */
	public static DeviceType detect(String userAgent) {
		if (userAgent == null) return UNKNOWN;
		
		// Detect Android
		if (userAgent.contains("Android"))
			return userAgent.contains("Mobile") ? PHONE : TABLET;

		if (userAgent.contains("iPad"))
			return TABLET;
		
		if (userAgent.contains("iPhone") || userAgent.contains("iPod"))
			return PHONE;
		
		return DESKTOP;
	}
}