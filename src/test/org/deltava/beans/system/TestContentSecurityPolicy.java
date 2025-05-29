package org.deltava.beans.system;

import junit.framework.TestCase;

public class TestContentSecurityPolicy extends TestCase {

	@SuppressWarnings("static-method")
	public void testAdd() {
		ContentSecurityPolicy csp = new ContentSecurityPolicy(false);
		csp.add(ContentSecurity.DEFAULT, "foo.bar");
		assertTrue(csp.getData().contains("foo.bar"));
	}
	
	@SuppressWarnings("static-method")
	public void testMultiple() {
		ContentSecurityPolicy csp = new ContentSecurityPolicy(false);
		csp.add(ContentSecurity.DEFAULT, "foo.bar");
		csp.add(ContentSecurity.DEFAULT, "bar.foo");
		assertTrue(csp.getData().contains("foo.bar"));
		assertTrue(csp.getData().contains("bar.foo"));
		assertFalse(csp.getData().contains("foo.foo"));
	}
	
	@SuppressWarnings("static-method")
	public void testheaderNames() {
		ContentSecurityPolicy csp = new ContentSecurityPolicy(false);
		assertEquals("Content-Security-Policy-Report-Only", csp.getHeader());
		
		csp = new ContentSecurityPolicy(true);
		assertEquals("Content-Security-Policy", csp.getHeader());
	}
}