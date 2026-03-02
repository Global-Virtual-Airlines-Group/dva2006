// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.dns;

import org.deltava.beans.RemoteAddressBean;
import org.deltava.util.cache.Cacheable;

/**
 * A bean to store a cached reverse DNS entry.
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

class DNSEntry implements RemoteAddressBean, Cacheable {
	
	private final String _addr;
	private final String _hostName;

	/**
	 * Creates the bean.
	 * @param addr the IP address
	 * @param hostName the host name 
	 */
	DNSEntry(String addr, String hostName) {
		super();
		_addr = addr;
		_hostName = hostName;
	}
	
	@Override
	public String getRemoteAddr() {
		return _addr;
	}

	@Override
	public String getRemoteHost() {
		return _hostName;
	}
	
	@Override
	public Object cacheKey() {
		return _addr;
	}

	@Override
	public int hashCode() {
		return _addr.hashCode();
	}
}