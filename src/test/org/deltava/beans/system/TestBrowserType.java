package org.deltava.beans.system;

import org.deltava.beans.system.BrowserType.BrowserVersion;

import junit.framework.TestCase;

public class TestBrowserType extends TestCase {

	@SuppressWarnings("static-method")
	public void testBrowserDetection() {
		
		assertType(BrowserType.SPIDER, "Mozilla/5.0 (compatible; SemrushBot/6~bl; +http://www.semrush.com/bot.html)");
		assertType(BrowserType.CHROME, "Mozilla/5.0 (Linux; Android 10; SM-G975U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.83 Mobile Safari/537.36");
		assertType(BrowserType.SPIDER, "Mozilla/5.0 (Linux; Android 6.0.1; Nexus 5X Build/MMB29P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.92 Mobile Safari/537.36 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
	}
	
	public static void assertType(BrowserType bt, String userAgent) {
		assertNotNull(userAgent);
		
		BrowserVersion bv = BrowserType.detect(userAgent);
		assertNotNull(bv);
		
		assertEquals(bt, bv.getType());
	}
}