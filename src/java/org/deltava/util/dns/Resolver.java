// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.dns;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.*;

import org.deltava.util.cache.*;

/**
 * A utility class to pass asynchronous reverse DNS requests to a separate daemon thread.
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

public class Resolver {
	
	private static final Cache<CacheableString> _cache = CacheManager.get(CacheableString.class, "ReverseDNS");
	
	private static final Logger log = LogManager.getLogger(Resolver.class);
	
	private static final AtomicLong _reqs = new AtomicLong();
	private static final AtomicLong _hits = new AtomicLong();

	// static class
	private Resolver() {
		super();
	}
	
	/**
	 * Returns the number of cache hits.
	 * @return the number of hits
	 */
	public static long getHits() {
		return _hits.longValue();
	}
	
	/**
	 * Returns the number of cache requests.
	 * @return the number of requests
	 */
	public static long getRequests() {
		return _reqs.longValue();
	}
	
	/**
	 * Resolves a host name from an IP address.
	 * @param addr the IP address
	 * @param wait the maximum time to wait in milliseconds
	 * @return the host name, or the IP address if it cannot be resolved or times out
	 */
	public static String resolve(String addr, int wait) {
		
		_reqs.incrementAndGet();
		CacheableString hostName = _cache.get(addr);
		if (hostName != null) {
			_hits.incrementAndGet();
			return hostName.getValue();
		}
		
		// Offer the address
		final String a = addr.intern();
		boolean isOK = ResolverDaemon.add(a);
		if (!isOK) {
			log.warn("Cannot accept reverse DNS request for {} - queue full", addr);
			return addr;
		}
		
		// Wait for the result
		try {
			log.debug("Resolinvg {}", a);
			synchronized (a) {
				a.wait(Math.min(wait, wait));
			}
		} catch (InterruptedException ie) {
			log.info("{} timed Out after {}ms", a, Integer.valueOf(wait));
			return addr;
		}
		
		hostName = _cache.get(addr);
		return (hostName == null) ? addr : hostName.getValue();
	}
}