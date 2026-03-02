// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.dns;

import java.net.*;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.*;

import org.deltava.util.TaskTimer;
import org.deltava.util.cache.*;

/**
 * A reverse DNS resolver worker task.
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

class ResolverWorker implements Callable<String> {
	
	private static final Cache<DNSEntry> _cache = CacheManager.get(DNSEntry.class, "ReverseDNS");
	private static final Logger log = LogManager.getLogger(ResolverWorker.class);
	
	private final String _addr;
	
	ResolverWorker(String addr) {
		super();
		_addr = addr;
	}

	@Override
	public String call() throws Exception {
		TaskTimer tt = new TaskTimer(); 
		try {
			InetAddress host = InetAddress.getByName(_addr);
			String hostName = host.getCanonicalHostName();
			_cache.add(new DNSEntry(_addr, hostName));
			return hostName;
		} catch (UnknownHostException uhe) {
			log.warn("Cannot Resolve {}", _addr);
			_cache.add(new DNSEntry(_addr, _addr));
		} finally {
			long ms = tt.stop();
			if (ms > 5100)
				log.warn("Slow reverse DNS resolution for {} - {}ms", _addr, Long.valueOf(ms));
		}
		
		return _addr;
	}
}