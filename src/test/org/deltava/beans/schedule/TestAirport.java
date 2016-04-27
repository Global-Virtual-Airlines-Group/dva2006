package org.deltava.beans.schedule;

import java.util.*;

import org.deltava.beans.TZInfo;
import org.hansel.CoverageDecorator;

import junit.framework.Test;
import junit.framework.TestCase;

public class TestAirport extends TestCase {
    
    private Airport _a;
    
    public static Test suite() {
        return new CoverageDecorator(TestAirport.class, new Class[] { Airport.class } );
    }

	@Override
	protected void setUp() throws Exception {
	    super.setUp();
	    _a = new Airport("ATL", "KATL", "Atlanta GA");
	}
	
	@Override
	protected void tearDown() throws Exception {
	    _a = null;
	    super.tearDown();
	}

	public void testConstructor() {
	    assertEquals(_a.getIATA(), "ATL");
	    assertEquals(_a.getICAO(), "KATL");
	    assertEquals(_a.getName(), "Atlanta GA");
	    assertTrue(_a.getTZ().getID().equals(TZInfo.UTC.getID()));
	    assertNotNull(_a.getPosition());
	    assertEquals(0, _a.getLatitude(), 0.0001);
	    assertEquals(0, _a.getLongitude(), 0.0001);
	    assertEquals(0, _a.getAltitude());
	    _a.setName("Atlanta, GA");
	    assertEquals(_a.getName(), "Atlanta GA");
	}
	
	public void testComboAlias() {
	    assertEquals(_a.getComboName(), _a.getName() + " (" + _a.getIATA() + ")");
	    assertEquals(_a.getComboAlias(), _a.getIATA());
	}
	
	public void testPosition() {
	    _a.setLocation(33.1234, -75.1243);
	    assertEquals(33.1234, _a.getLatitude(), 0.0001);
	    assertEquals(-75.1243, _a.getLongitude(), 0.0001);
	}
	
	public void testTZUpdate() {
	    TZInfo cst = TZInfo.init("US/Central", null, null);
	    _a.setTZ(cst);
	    assertTrue(cst.equals(_a.getTZ()));
	    _a.setTZ(cst);
	    assertTrue(cst.equals(_a.getTZ()));
	}
	
	@SuppressWarnings("static-method")
	public void testAllAirports() {
		Airport a = Airport.ALL;
		assertEquals("$AL", a.getIATA());
		assertEquals("$ALL", a.getICAO());
		assertEquals("All Airports", a.getComboName());
	}
	
	public void testComparator() {
	    Airport a2 = new Airport("CLT", "KCLT", "Charlotte NC");
	    assertTrue(_a.compareTo(a2) < 0);
	    assertTrue(a2.compareTo(_a) > 0);
	    assertFalse(a2.equals(_a));
	    assertFalse(_a.equals(new Object()));
	    
	    // Test equality on just IATA/ICAO
	    Airport a3 = new Airport("ATL", "KATL", "Atlanta");
	    Airport a4 = new Airport("ATL", "KATA", "Atlanta");	    
	    assertTrue(_a.equals(a3));
	    assertFalse(_a.equals(a4));
	    assertEquals(0, _a.compareTo(a3));
	}
	
	public void testAirlineCodes() {
	    assertNotNull(_a.getAirlineCodes());
	    assertEquals(0, _a.getAirlineCodes().size());
	    _a.addAirlineCode("DAL");
	    _a.addAirlineCode("af");
	    _a.addAirlineCode("DAL");
	    assertEquals(2, _a.getAirlineCodes().size());
	    assertTrue(_a.hasAirlineCode("DAL"));
	    assertTrue(_a.hasAirlineCode("AF"));
	    
	    Set<String> aCodes = new HashSet<String>();
	    aCodes.add("NWA");
	    aCodes.add("coa");
	    _a.setAirlines(aCodes);
	    assertEquals(2, _a.getAirlineCodes().size());
	    assertTrue(_a.hasAirlineCode("NWA"));
	    assertTrue(_a.hasAirlineCode("COA"));
	}
	
	public void testHashCode() {
	   assertEquals((_a.getIATA() + _a.getICAO()).hashCode() , _a.hashCode());
	}
	
	public void testToString() {
	   assertEquals("Atlanta GA (ATL)", _a.toString());
	}
}