package org.deltava.beans.schedule;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

@Deprecated
public class TestPreferredRoute extends AbstractBeanTestCase {

    private PreferredRoute _pr;
    
    public static Test suite() {
        return new CoverageDecorator(TestPreferredRoute.class, new Class[] { PreferredRoute.class } );
    }
    
    protected void tearDown() throws Exception {
        _pr = null;
        super.tearDown();
    }

    public void testProperties() {
        Airport aa = new Airport("JFK", "KJFK", "New York NY");
        Airport ad = new Airport("ATL", "KATL", "Atlanta GA");
        _pr = new PreferredRoute(ad, aa);
        setBean(_pr);
        assertEquals(aa, _pr.getAirportA());
        assertEquals(ad, _pr.getAirportD());
        checkProperty("route", "ATL ODK SAX JFK");
        checkProperty("ARTCC", "ZTL ZDC ZNY");
    }
    
    public void testValidation() {
        _pr = new PreferredRoute(new Airport("ATL", "KATL", "Atlanta GA"), new Airport("JFK", "KJFK", "New York NY"));
        setBean(_pr);
        validateInput("route", null, NullPointerException.class);
        validateInput("ARTCC", null, NullPointerException.class);
    }
    
    public void testComparator() {
        Airport aa = new Airport("JFK", "KJFK", "New York NY");
        Airport aa2 = new Airport("LGA", "KLGA", "New York NY");
        Airport ad = new Airport("ATL", "KATL", "Atlanta GA");
        Airport ad2 = new Airport("SAV", "KSAV", "Savannah GA");
        _pr = new PreferredRoute(ad, aa);
        PreferredRoute pr2 = new PreferredRoute(ad, aa2);
        PreferredRoute pr3 = new PreferredRoute(ad2, aa);
        assertTrue(_pr.compareTo(pr2) < 0);
        assertTrue(pr2.compareTo(pr3) < 0);
    }
}