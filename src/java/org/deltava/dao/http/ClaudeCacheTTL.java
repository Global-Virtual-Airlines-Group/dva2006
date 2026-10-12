// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

/**
 * An enumeration of Claude API cache TTLs.
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

enum ClaudeCacheTTL {
	NONE(null), SHORT("5m"), LONG("1h");
	
	private String _value;
	
	ClaudeCacheTTL(String value) {
		_value = value;
	}
	
	/**
	 * Returns the cache TTL value
	 * @return the value
	 */
	String getValue() {
		return _value;
	}
}