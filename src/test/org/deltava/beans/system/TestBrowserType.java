package org.deltava.beans.system;

import org.deltava.beans.system.BrowserType.BrowserVersion;

import junit.framework.TestCase;

public class TestBrowserType extends TestCase {

	@SuppressWarnings("static-method")
	public void testBrowserDetection() {
		
		assertType(BrowserType.SPIDER, "Mozilla/5.0 (compatible; SemrushBot/6~bl; +http://www.semrush.com/bot.html)");
		assertType(BrowserType.CHROME, "Mozilla/5.0 (Linux; Android 10; SM-G975U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.83 Mobile Safari/537.36");	
	}
	
	public static void assertType(BrowserType bt, String userAgent) {
		assertNotNull(userAgent);
		
		BrowserVersion bv = BrowserType.detect(userAgent);
		assertNotNull(bv);
		
		assertEquals(bt, bv.getType());
	}
}
