package org.deltava.util;

import junit.framework.Test;
import junit.framework.TestCase;

import org.hansel.CoverageDecorator;

public class TestThreadUtils extends TestCase {
	
	private SleepyThread _st;

	public static Test suite() {
		return new CoverageDecorator(TestThreadUtils.class, new Class[] { ThreadUtils.class });
	}
	
	private class SleepyThread extends Thread {
		
		private int _sleepTime;
		
		public SleepyThread(int sleepTime) {
			super("Sleepy Thread");
			_sleepTime = sleepTime;
		}
		
		public void run() {
			try {
				Thread.sleep(_sleepTime);
			} catch (InterruptedException ie) {
			}
		}
	}

	protected void tearDown() throws Exception {
		if ((_st != null) && (_st.isAlive()))
			_st.join();
		
		_st = null;
		super.tearDown();
	}

	public void testIsAlive() throws Exception {
		assertFalse(ThreadUtils.isAlive((Thread) null));
		_st = new SleepyThread(100);
		assertNotNull(_st);
		assertFalse(ThreadUtils.isAlive(_st));
		
		_st.start();
		assertTrue(ThreadUtils.isAlive(_st));
		
		Thread.sleep(120);
		assertNotNull(_st);
		assertFalse(ThreadUtils.isAlive(_st));
	}
	
	public void testSleep() {
		long now = System.currentTimeMillis();
		ThreadUtils.sleep(50);
		assertTrue((System.currentTimeMillis() - now) >= 50);
	}
	
	public void testKill() {
		_st = new SleepyThread(400);
		_st.start();
		assertTrue(ThreadUtils.isAlive(_st));
		
		ThreadUtils.kill(_st, 50);
		assertNotNull(_st);
		assertFalse(ThreadUtils.isAlive(_st));
		
		ThreadUtils.kill(_st, 50);
		ThreadUtils.kill((Thread) null, 50);
	}
}