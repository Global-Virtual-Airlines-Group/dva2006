// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.dns;

import java.net.*;
import java.util.concurrent.*;

import org.apache.logging.log4j.*;

import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A daemon to handle asynchronous resverse DNS requests. 
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

public class ResolverDaemon implements Runnable {
	
	private static final Logger log = LogManager.getLogger(ResolverDaemon.class);

	private static final Cache<CacheableString> _cache = CacheManager.get(CacheableString.class, "ReverseDNS");
	private static final BlockingQueue<String> _work = new ArrayBlockingQueue<String>(8);
	
	/**
	 * Adds a work item for the daemon. The daemon can refuse work if it has too many entries in the queue.
	 * @param addr the IP address
	 * @return TRUE if the queue is not backed up, otherwise FALSE
	 */
	static boolean add(String addr) {
		return _work.offer(addr);
	}
	
	@Override
	public String toString() {
		return SystemData.get("airline.code") + " Resolver Daemon";
	}
	
	@Override
	public void run() {
		log.info("Started");
		while (!Thread.currentThread().isInterrupted()) {
			try {
				String addr = _work.take();
				try {
					InetAddress host = InetAddress.getByName(addr);
					log.debug("{} resolves to {}", addr, host.getCanonicalHostName());
					_cache.add(new CacheableString(addr, host.getCanonicalHostName()));
				} catch (UnknownHostException uhe) {
					log.warn("Cannot Resolve {}", addr);
					_cache.add(new CacheableString(addr, addr));
				}
				
				// Notify
				synchronized(addr) {
					addr.notify();
				}
			} catch (InterruptedException ie) {
				log.warn("Interrupted");
			}
		}
		
		log.info("Stopped");
	}
}