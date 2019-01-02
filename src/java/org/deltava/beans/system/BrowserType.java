// Copyright 2011, 2012, 2013, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.system;

import java.util.*;

/**
 * An enumeration to store browser family data.
 * @author Luke
 * @version 8.5
 * @since 3.7
 */

public enum BrowserType {
	
	IE("MSIE", "Trident"), CHROME("Chrome"), FIREFOX("Firefox"), WEBKIT("WebKit", "Safari"), OPERA("Opera"), 
	SPIDER("Googlebot", "bingbot", "YandexBot", "ezooms.bot", "msnbot", "MJ12bot", "Baiduspider",  "Yahoo! Slurp",  "SemrushBot"), UNKNOWN;
	
	private final Collection<String> _detectStrings = new ArrayList<String>(4);
	
	public static class BrowserVersion {
		private final BrowserType _browser;
		private final String _version;
		
		BrowserVersion() {
			this(UNKNOWN, "0.0");
		}
		
		BrowserVersion(BrowserType bt, String version) {
			_browser = bt;
			_version = version;
		}
		
		public BrowserType getType() {
			return _browser;
		}
		
		public String getVersion() {
			return _version;
		}
	}

	BrowserType(String... detectStrings) {
		_detectStrings.addAll(Arrays.asList(detectStrings));
	}
	
	/**
	 * Detects a browser family based on a user agent header.
	 * @param userAgent the user agent header value
	 * @return a BrowserType
	 */
	public static BrowserVersion detect(String userAgent) {
		if (userAgent == null)
			return new BrowserVersion();
		
		for (int x = 0; x < values().length - 1; x++) {
			BrowserType bType = BrowserType.values()[x];
			for (String keyword : bType._detectStrings) {
				int pos = userAgent.indexOf(keyword);
				if (pos != -1) {
					pos += keyword.length();
					
					// If IE11, search for rv:
					if (bType == BrowserType.IE)
						pos = Math.max(pos, userAgent.indexOf("rv:") + 2);
					
					// Get version string
					StringBuilder buf = new StringBuilder();
					boolean hasMinor = false;
					for (int y = pos + 1; y < userAgent.length(); y++) {
						char c = userAgent.charAt(y);
						if (Character.isDigit(c))
							buf.append(c);
						else if (!hasMinor && (c == '.')) {
							hasMinor = true;
							buf.append('.');
						} else
							break;
					}
					
					if (!hasMinor)
						buf.append(".0");
					
					return new BrowserVersion(bType, buf.toString());
				}
			}
		}
		
		return new BrowserVersion();
	}
}