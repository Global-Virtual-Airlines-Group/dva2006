package org.deltava.util;

import junit.framework.TestCase;

public class TestCIDRBlock extends TestCase {

	@SuppressWarnings("static-method")
	public void testIPv4() {
		
		CIDRBlock cb = new CIDRBlock("205.206.207.0/24");
		assertNotNull(cb);
		assertFalse(cb.isIPv6());
		assertEquals("205.206.207.0", cb.getNetworkAddress());
		assertEquals("205.206.207.255", cb.getBroadcastAddress());
		assertEquals(24, cb.getPrefixLength());
		assertTrue(cb.isInRange("205.206.207.208"));
		assertFalse(cb.isInRange("205.206.208.2"));
		
		cb = new CIDRBlock("20.0.0.0/8");
		assertNotNull(cb);
		assertFalse(cb.isIPv6());
		assertEquals("20.0.0.0", cb.getNetworkAddress());
		assertEquals("20.255.255.255", cb.getBroadcastAddress());
		assertEquals(8, cb.getPrefixLength());
		assertTrue(cb.isInRange("20.206.208.2"));
		assertFalse(cb.isInRange("205.206.207.208"));
	}
	
	@SuppressWarnings("static-method")
	public void testIPv6() {
		
		CIDRBlock cb = new CIDRBlock("2600:1702:1621:3cbf::2/64");
		assertNotNull(cb);
		assertTrue(cb.isIPv6());
		assertEquals("2600:1702:1621:3cbf:0:0:0:0", cb.getNetworkAddress());
		assertEquals("2600:1702:1621:3cbf:ffff:ffff:ffff:ffff", cb.getBroadcastAddress());
		assertEquals(64, cb.getPrefixLength());
		assertTrue(cb.isInRange("2600:1702:1621:3cbf::54"));
		assertFalse(cb.isInRange("2600:1702:1621:3cc0::1"));
		
		cb = new CIDRBlock("fe80::/10");
		assertNotNull(cb);
		assertTrue(cb.isIPv6());
		assertEquals("fe80:0:0:0:0:0:0:0", cb.getNetworkAddress());
		assertEquals("febf:ffff:ffff:ffff:ffff:ffff:ffff:ffff", cb.getBroadcastAddress());
		assertEquals(10, cb.getPrefixLength());
		assertTrue(cb.isInRange("fe80::224:8cff:fe75:1325"));
		assertFalse(cb.isInRange("2600:1702:1621:3cc0::1"));
	}
}