package org.deltava.beans.schedule;

import org.hansel.CoverageDecorator;

import junit.framework.Test;
import junit.framework.TestCase;

public class TestAirline extends TestCase {

	private Airline _a;

   public static Test suite() {
       return new CoverageDecorator(TestAirline.class, new Class[] { Airline.class } );
   }
	
	protected void tearDown() throws Exception {
		_a = null;
		super.tearDown();
	}

	public void testProperties() {
		_a = new Airline("DAL", "Delta Air Lines");
		assertEquals("DAL", _a.getCode());
		assertEquals("Delta Air Lines", _a.getName());
		assertEquals(_a.getCode(), _a.getComboAlias());
		assertEquals(_a.getName(), _a.getComboName());
		assertEquals(_a.getCode(), _a.cacheKey());
		assertEquals(_a.getCode().hashCode(), _a.hashCode());
		assertTrue(_a.getActive());
		_a.setActive(false);
		assertFalse(_a.getActive());
		
	    Airline a2  = new Airline("DAL");
	    assertEquals("", a2.getName());
	    a2.setName("Delta");
	    assertEquals("Delta", a2.getName());
	}

	public void testConstructor() {
		_a = new Airline("dal", "Delta");
		assertEquals("DAL", _a.getCode());
		try {
			Airline a2 = new Airline(null, "DAL");
			fail("Expected NullPointerException");
			assertNotNull(a2);
		} catch (NullPointerException npe) { 
			// empty
		}
		
		try {
			Airline a2 = new Airline("DAL", null);
			fail("Expected NullPointerException");
			assertNotNull(a2);
		} catch (NullPointerException npe) {
			// empty
		}
	}
	
	public void testComparator() {
	    _a = new Airline("DAL", "Delta Air Lines");
	    Airline a2 = new Airline("AF", "Air France");
	    Airline a3 = new Airline("DAL", "Delta");
	    assertTrue(_a.compareTo(a2) > 0);
	    assertTrue(a2.compareTo(_a) < 0);
	    assertTrue(_a.equals(a3));
	    assertFalse(_a.equals(a2));
	    assertFalse(_a.equals(new Object()));
	    assertFalse(_a.equals(null));
	}
}