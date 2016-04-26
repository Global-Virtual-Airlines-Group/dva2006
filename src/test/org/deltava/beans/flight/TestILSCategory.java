package org.deltava.beans.flight;

import junit.framework.TestCase;

public class TestILSCategory extends TestCase {
	
	@SuppressWarnings("static-method")
	public void testCategory() {
		
		assertEquals(ILSCategory.NONE, ILSCategory.categorize(3000, 4500));
		assertEquals(ILSCategory.CATI, ILSCategory.categorize(1000, 2500));
	}
}