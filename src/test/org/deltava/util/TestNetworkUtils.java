package org.deltava.util;

import java.net.InetAddress;

import junit.framework.TestCase;

public class TestNetworkUtils extends TestCase {

	@SuppressWarnings("static-method")
	public void testFormat() throws Exception {
		String host = "208.89.100.89";
		InetAddress addr = InetAddress.getByName(host);
		assertNotNull(addr);
		assertEquals(host, NetworkUtils.format(addr.getAddress()));
	}
}