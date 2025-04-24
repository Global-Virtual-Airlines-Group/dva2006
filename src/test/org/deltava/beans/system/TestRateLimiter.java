package org.deltava.beans.system;

import org.deltava.util.CIDRBlock;

import junit.framework.TestCase;

public class TestRateLimiter extends TestCase {
	
	private static final String ADDRBLOCK = "1.2.0.0/16";
	private static final String ADDR1 = "1.2.3.4";
	private static final String ADDR2 = "1.2.3.76";
	
	@SuppressWarnings("static-method")
	public void testAddress1() {
		
		RateLimiter rl = new RateLimiter(false, 5, 3600);
		rl.setBlocking(10, 600);
		
		RateLimiter.Result r = rl.addAddress(ADDR1);
		assertEquals(1, rl.getCounters().size());
		assertEquals(RateLimiter.Result.PASS, r);
		
		for (int x = 0; x < 3; x++) {
			r = rl.addAddress(ADDR1);
			assertEquals(RateLimiter.Result.PASS, r);
		}
		
		r = rl.addAddress(ADDR1);
		assertEquals(RateLimiter.Result.DEGRADE, r);
		RequestCounter rc = rl.get(ADDR1);
		assertNotNull(rc);
		assertEquals(ADDR1, rc.getAddress());
		
		for (int x = 0; x < 5; x++) {
			r = rl.addAddress(ADDR1);
			assertEquals(RateLimiter.Result.DEGRADE, r);
		}

		r = rl.addAddress(ADDR1);
		assertEquals(RateLimiter.Result.BLOCK, r);
	}
	
	@SuppressWarnings("static-method")
	public void testAddress2() {
		
		RateLimiter rl = new RateLimiter(false, 5, 3600);
		rl.setBlocking(10, 600);
		
		RateLimiter.Result r = rl.addAddress(ADDR1);
		assertEquals(1, rl.getCounters().size());
		assertEquals(RateLimiter.Result.PASS, r);
		
		r = rl.addAddress(ADDR2);
		assertEquals(2, rl.getCounters().size());
		assertEquals(RateLimiter.Result.PASS, r);
		
		rl.merge();
		assertEquals(2, rl.getCounters().size());
	}
	
	@SuppressWarnings("static-method")
	public void testMerge() {
		
		// Validate CIDR
		CIDRBlock cidr = new CIDRBlock(ADDRBLOCK);
		assertNotNull(cidr);
		assertTrue(cidr.isInRange(ADDR1));
		assertTrue(cidr.isInRange(ADDR2));
		
		// Create the network block
		IPBlock ip = new IPBlock(1, ADDRBLOCK);
		
		RateLimiter rl = new RateLimiter(true, 2, 3600);
		rl.setBlocking(10, 600);

		RateLimiter.Result r = rl.addAddress(ADDR1);
		assertEquals(1, rl.getCounters().size());
		assertEquals(RateLimiter.Result.PASS, r);

		// Since there is no netblock associated with the counter for ADDR1, this will not merge
		r = rl.addAddress(ADDR2);
		assertEquals(2, rl.getCounters().size());
		assertEquals(RateLimiter.Result.PASS, r);
		
		// Assign the netblock
		rl.getCounters().forEach(rc -> rc.setIPInfo(ip));

		// Merge, and validate the merge
		rl.merge();
		assertEquals(1, rl.getCounters().size());
		
		r = rl.addAddress(ADDR2);
		assertEquals(1, rl.getCounters().size());
		assertEquals(RateLimiter.Result.DEGRADE, r);

		RequestCounter rc1 = rl.get(ADDR1);
		assertNotNull(rc1);
		RequestCounter rc2 = rl.get(ADDR2);
		assertNotNull(rc2);
		assertSame(rc1, rc2);
		
		RequestCounter rc = rl.get(cidr.getNetworkAddress());
		assertNotNull(rc);
		assertSame(rc, rc1);
		assertEquals(3, rc.getRequests());
		assertEquals(cidr.getNetworkAddress(), rc.getAddress());
	}
}