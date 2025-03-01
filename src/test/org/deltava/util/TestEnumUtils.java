package org.deltava.util;

import junit.framework.TestCase;

public class TestEnumUtils extends TestCase {
	
	private enum E {
		FIRST, SECOND, THIRD;
	}

	@SuppressWarnings("static-method")
	public void testParse() {
		assertEquals(E.FIRST, EnumUtils.parse(E.class, "FIRST", null));
		assertEquals(E.THIRD, EnumUtils.parse(E.class, "THIRD", null));
		assertEquals(E.FIRST, EnumUtils.parse(E.class, "FOO", E.FIRST));
		assertEquals(E.THIRD, EnumUtils.parse(E.class, null, E.THIRD));
	}
	
	@SuppressWarnings("static-method")
	public void testMax() {
		assertEquals(E.THIRD, EnumUtils.max(E.FIRST, E.THIRD));
		assertEquals(E.SECOND, EnumUtils.max(E.FIRST, E.SECOND));
		assertEquals(E.THIRD, EnumUtils.max(E.SECOND, E.THIRD));
		assertEquals(E.THIRD, EnumUtils.max(E.THIRD, E.THIRD));
	}
}