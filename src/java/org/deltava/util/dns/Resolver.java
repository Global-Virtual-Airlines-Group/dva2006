// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.dns;

import java.util.concurrent.*;
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
	
	private static final Cache<DNSEntry> _cache = CacheManager.get(DNSEntry.class, "ReverseDNS");
	private static final Logger log = LogManager.getLogger(Resolver.class);
	
	private static final BlockingQueue<Runnable> _work = new ArrayBlockingQueue<Runnable>(24);
	private static final ThreadPoolExecutor _exec = new ThreadPoolExecutor(1, 8, 2500, TimeUnit.MILLISECONDS, _work, Thread.ofVirtual().factory());
	
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
	
	public static void start() {
		_exec.allowCoreThreadTimeOut(true);
		_exec.prestartCoreThread();
		log.info("Started");
	}
	
	/**
	 * Shuts down the executor pool.
	 */
	public static void stop() {
		log.info("Stopping");
		_exec.shutdownNow();
		log.info("Stopped - {} hits, {} requests", Long.valueOf(_hits.longValue()), Long.valueOf(_reqs.longValue()));
	}
	
	/**
	 * Resolves a host name from an IP address.
	 * @param addr the IP address
	 * @param wait the maximum time to wait in milliseconds
	 * @return the host name, or the IP address if it cannot be resolved or times out
	 */
	public static String resolve(String addr, int wait) {
		
		_reqs.incrementAndGet();
		DNSEntry de = _cache.get(addr);
		if (de != null) {
			_hits.incrementAndGet();
			return de.getRemoteHost();
		}
		
		// Wait for the result
		log.debug("Resolinvg {}", addr);
		try {
			Future<String> f = _exec.submit(new ResolverWorker(addr));
			String hostName = f.get(Math.min(wait, 2500), TimeUnit.MILLISECONDS);
			log.debug("{} resolves to {}", addr, hostName);
			return hostName;
		} catch (InterruptedException | TimeoutException ie) {
			log.info("{} timed Out after {}ms", addr, Integer.valueOf(wait));
		} catch (RejectedExecutionException re) {
			log.error("Cannot resolve {} - queue full", addr);
		} catch (ExecutionException ee) {
			log.atError().withThrowable(ee.getCause()).log("Error resolving {} - {}", addr, ee.getMessage());
		}
		
		return addr;
	}
}