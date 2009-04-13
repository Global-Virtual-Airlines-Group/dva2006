package org.deltava.util;

import java.net.InetAddress;

import junit.framework.TestCase;

import static org.deltava.util.NetworkUtils.NetworkType.*;

public class TestNetworkUtils extends TestCase {

	public void testConvert() throws Exception {
		String host = "208.89.100.89";
		InetAddress addr = InetAddress.getByName(host);
		assertNotNull(addr);
		
		byte[] b = addr.getAddress();
		long rawAddr = NetworkUtils.convertIP(b);
		byte[] b2 = NetworkUtils.convertIP(rawAddr);
		assertEquals(b[0], b2[0]);
		assertEquals(b[1], b2[1]);
		assertEquals(b[2], b2[2]);
		assertEquals(b[3], b2[3]);
	}
	
	public void testMask() {
		long mask = NetworkUtils.pack("255.255.255.0");
		assertEquals(0xFFFFFF00, mask);
		assertEquals("FFFFFF00", Long.toHexString(mask).toUpperCase());
		
		long myBase = NetworkUtils.pack("208.89.88.0");
		assertEquals(myBase, NetworkUtils.pack("208.89.88.16") & mask);
		
		mask = NetworkUtils.pack("255.255.224.0");
		assertEquals(8192, (~mask + 1));
		assertEquals("FFFFE000", Long.toHexString(mask).toUpperCase());
	}
	
	public void testFormat() throws Exception {
		String host = "208.89.100.89";
		InetAddress addr = InetAddress.getByName(host);
		assertNotNull(addr);
		assertEquals(host, NetworkUtils.format(addr.getAddress()));
	}
	
	public void testNetworkType() throws Exception {
		assertEquals(A, NetworkUtils.getNetworkType(InetAddress.getByName("32.15.26.253").getAddress()));
		assertEquals(B, NetworkUtils.getNetworkType(InetAddress.getByName("131.215.126.251").getAddress()));
		assertEquals(C, NetworkUtils.getNetworkType(InetAddress.getByName("192.168.0.1").getAddress()));
		assertEquals(D, NetworkUtils.getNetworkType(InetAddress.getByName("225.1.0.1").getAddress()));
		assertEquals(E, NetworkUtils.getNetworkType(InetAddress.getByName("242.1.0.1").getAddress()));
	}
}