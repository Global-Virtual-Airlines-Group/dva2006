// Copyright 2011, 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.util.*;

/**
 * An enumeration to store client operating system data.
 * @author Luke
 * @version 5.2
 * @since 3.7
 */

public enum OperatingSystem {
	
	WINDOWS("Windows"), OSX("MAC OS"), LINUX("Linux", "BSD"), IOS("iPad", "iPhone", "iPod"), ANDROID("Android"), UNKNOWN;
	
	private final Collection<String> _detectStrings = new LinkedHashSet<String>(4);
	
	OperatingSystem(String... detectStrings) {
		_detectStrings.addAll(Arrays.asList(detectStrings));
	}
	
	/**
	 * Detects an operating system based on a user agent header.
	 * @param userAgent the user agent header value
	 * @return an OperatingSystem
	 */
	public static OperatingSystem detect(String userAgent) {
		for (int x = 0; x < OperatingSystem.values().length - 1; x++) {
			OperatingSystem os = OperatingSystem.values()[x];
			for (String keyword : os._detectStrings) {
				if (userAgent.contains(keyword))
					return os;
			}
		}
		
		return OperatingSystem.UNKNOWN;
	}
}