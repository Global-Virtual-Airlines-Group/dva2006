// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.dns;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.*;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.*;

/**
 * A utility class to pass asynchronous reverse DNS requests to a separate daemon thread.
 * @author Luke
 * @version 12.5
 * @since 12.4
 */

public class Resolver {
	
	private final Cache<DNSEntry> _cache = CacheManager.get(DNSEntry.class, "ReverseDNS");
	private static final Logger log = LogManager.getLogger(Resolver.class);
	
	private static final BlockingQueue<Runnable> _work = new ArrayBlockingQueue<Runnable>(32);
	private final ThreadPoolExecutor _exec = new ThreadPoolExecutor(1, 8, 2500, TimeUnit.MILLISECONDS, _work, Thread.ofVirtual().name("DNS Worker").factory());
	
	private final AtomicLong _hits = new AtomicLong();
	private final AtomicLong _reqs = new AtomicLong();
	private final AtomicLong _errs = new AtomicLong();
	
	/**
	 * Starts the executor pool.
	 */
	public void start() {
		_exec.allowCoreThreadTimeOut(true);
		_exec.prestartCoreThread();
		log.info("Started - {} threads", Integer.valueOf(_exec.getMaximumPoolSize()));
	}
	
	/**
	 * Returns the number of currently active resolver threads.
	 * @return the number of threads
	 */
	public int getThreadCount() {
		return _exec.getActiveCount();
	}
	
	/**
	 * Returns the number of resolver cache hits. If this cache is shared, calling this method on the cache will return
	 * the aggregate across all cache consumers, so this class maintains its own counter.
	 * @return the number of cache hits
	 */
	public long getHits() {
		return _hits.longValue();
	}
	
	/**
	 * Returns the number of resolver cache requests. If this cache is shared, calling this method on the cache will return
	 * the aggregate across all cache consumers, so this class maintains its own counter.
	 * @return the number of cache requests
	 */
	public long getRequests() {
		return _reqs.longValue();
	}
	
	/**
	 * Returns the number of rejected resolver entries. If this cache is shared, calling this method on the cache will return
	 * the aggregate across all cache consumers, so this class maintains its own counter.
	 * @return the number of rejected requests 
	 */
	public long getRejected() {
		return _errs.longValue();
	}
	
	/**
	 * Returns the cache hit ratio.
	 * @return the ratio from 0 to 1
	 */
	public float getHitRate() {
		return (getRequests() == 0) ? 0 : getHits() * 1f / getRequests();
	}
	
	/**
	 * Shuts down the executor pool.
	 */
	public void stop() {
		_exec.shutdownNow();
		log.info("Stopped - {} hits, {} requests ( {} )", Long.valueOf(getHits()), Long.valueOf(getRequests()), StringUtils.format(getHitRate(), "##0.00%"));
		log.info("Maximum threads - {}, Queue Full = {}", Integer.valueOf(_exec.getLargestPoolSize()), Long.valueOf(getRejected()));
	}
	
	/**
	 * Resolves a host name from an IP address.
	 * @param addr the IP address
	 * @param wait the maximum time to wait in milliseconds
	 * @return the host name, or the IP address if it cannot be resolved or times out
	 */
	public String resolve(String addr, int wait) {
		
		// Check the cache
		_reqs.incrementAndGet();
		DNSEntry de = _cache.get(addr);
		if (de != null) {
			_hits.incrementAndGet();
			return de.getRemoteHost();
		}
		
		// Wait for the result
		final int w = Math.min(wait, 2500);
		log.debug("Resolinvg {}", addr);
		try {
			Future<String> f = _exec.submit(new ResolverWorker(addr));
			String hostName = f.get(w, TimeUnit.MILLISECONDS);
			log.debug("{} resolves to {}", addr, hostName);
			return hostName;
		} catch (InterruptedException | TimeoutException ie) {
			log.debug("{} timed out after {}ms", addr, Integer.valueOf(w));
		} catch (RejectedExecutionException re) {
			_errs.incrementAndGet();
			log.warn("Cannot resolve {} - queue full", addr);
			_cache.add(new DNSTimeoutEntry(addr, 300));
		} catch (ExecutionException ee) {
			log.atError().withThrowable(ee.getCause()).log("Error resolving {} - {}", addr, ee.getMessage());
		}
		
		return addr;
	}
}