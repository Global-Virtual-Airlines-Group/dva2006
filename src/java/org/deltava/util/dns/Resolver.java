// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.dns;

import java.util.concurrent.*;

import org.apache.logging.log4j.*;
import org.deltava.util.StringUtils;
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
	private static final ThreadPoolExecutor _exec = new ThreadPoolExecutor(1, 4, 2500, TimeUnit.MILLISECONDS, _work, Thread.ofVirtual().name("DNS Worker").factory());
	
	// static class
	private Resolver() {
		super();
	}
	
	public static void start() {
		_exec.allowCoreThreadTimeOut(true);
		_exec.prestartCoreThread();
		log.info("Started - {} threads", Integer.valueOf(_exec.getMaximumPoolSize()));
	}
	
	/**
	 * Shuts down the executor pool.
	 */
	public static void stop() {
		log.info("Stopping");
		_exec.shutdownNow();
		double hitRate = (_cache.getRequests() == 0) ? 0 : _cache.getHits() * 1d / _cache.getRequests();
		log.info("Stopped - {} hits, {} requests ( {} )", Long.valueOf(_cache.getHits()), Long.valueOf(_cache.getRequests()), StringUtils.format(hitRate, "##0.00%"));
		log.info("Maximum threads - {}", Integer.valueOf(_exec.getLargestPoolSize()));
	}
	
	/**
	 * Resolves a host name from an IP address.
	 * @param addr the IP address
	 * @param wait the maximum time to wait in milliseconds
	 * @return the host name, or the IP address if it cannot be resolved or times out
	 */
	public static String resolve(String addr, int wait) {
		
		// Check the cache
		DNSEntry de = _cache.get(addr);
		if (de != null)
			return de.getRemoteHost();
		
		// Wait for the result
		final int w = Math.min(wait, 2500);
		log.debug("Resolinvg {}", addr);
		try {
			Future<String> f = _exec.submit(new ResolverWorker(addr));
			String hostName = f.get(w, TimeUnit.MILLISECONDS);
			log.debug("{} resolves to {}", addr, hostName);
			return hostName;
		} catch (InterruptedException | TimeoutException ie) {
			log.info("{} timed out after {}ms", addr, Integer.valueOf(w));
		} catch (RejectedExecutionException re) {
			log.warn("Cannot resolve {} - queue full", addr);
		} catch (ExecutionException ee) {
			log.atError().withThrowable(ee.getCause()).log("Error resolving {} - {}", addr, ee.getMessage());
		}
		
		return addr;
	}
}