// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.dns;

import java.time.Instant;

import org.deltava.util.cache.ExpiringCacheable;

/**
 * A bean to store a cached timed out reverse DNS entry. This is designed to provide a shorter TTL for invalid entries.
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

class DNSTimeoutEntry extends DNSEntry implements ExpiringCacheable {
	private final long _expiryTime;
	
	/**
	 * Creates the bean.
	 * @param addr the IP address
	 * @param ttl the cache TTL in seconds
	 */
	DNSTimeoutEntry(String addr, int ttl) {
		super(addr, addr);
		_expiryTime = System.currentTimeMillis() + (ttl * 1000);
	}

	@Override
	public Instant getExpiryDate() {
		return Instant.ofEpochMilli(_expiryTime);
	}
}