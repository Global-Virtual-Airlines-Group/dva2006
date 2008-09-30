package org.deltava.beans;

import java.util.*;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.schedule.Airport;

public class TestPerson extends AbstractBeanTestCase {
	
	public static Test suite() {
		return new CoverageDecorator(TestPerson.class, new Class[] { Person.class } );
   }

    private Person _p;
    
    // Since Person is an abstract class we merely extend it with no new methods except setStatus()
    // which is required to be called
    private class MockPerson extends Person {

        private List<String> _roles = new ArrayList<String>();
        
        MockPerson(String fName, String lName) {
            super(fName, lName);
        }
        
        public String getStatusName() {
        	return "MockPerson";
        }
        
        public final void setStatus(int status) {
            super.setStatus(status);
        }
        
        public void addRole(String roleName) {
            _roles.add(roleName);
        }
        
        public Collection<String> getRoles() {
            return _roles;
        }
        
        public boolean isInRole(String roleName) {
            return _roles.contains(roleName);
        }
        
        public String getRowClassName() {
        	return null;
        }
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        _p = new MockPerson("John", "Smith");
        setBean(_p);
    }
    
    protected void tearDown() throws Exception {
        _p = null;
        super.tearDown();
    }
    
    public void testProperties() {
        assertEquals("John", _p.getFirstName());
        assertEquals("Smith", _p.getLastName());
        assertEquals("John Smith", _p.getName());
        checkProperty("DN", "cn=John Smith");
        checkProperty("password", "password!!");
        checkProperty("email", "postmaster@dev.null");
        checkProperty("rank", "Captain");
        checkProperty("equipmentType", "CRJ-200");
        checkProperty("location", "Southeastern US");
        checkProperty("homeAirport", "ATL");
        checkProperty("airportCodeType", new Integer(1));
        assertEquals(Airport.CODETYPES[_p.getAirportCodeType()], _p.getAirportCodeTypeName());
        _p.setAirportCodeType(Airport.CODETYPES[0]);
        assertEquals(0, _p.getAirportCodeType());
        checkProperty("emailAccess", new Integer(1));
        checkProperty("legacyHours", new Double(238.1));
        checkProperty("createdOn", new Date());
        checkProperty("lastLogin", new Date());
        checkProperty("lastLogoff", new Date());
        checkProperty("loginCount", new Integer(12));
        checkProperty("loginHost", "localhost");
        checkProperty("status", new Integer(2));
        checkProperty("dateFormat", "MM/dd/yyyy");
        checkProperty("timeFormat", "hh:mm:ss");
        checkProperty("numberFormat", "#,##0.0");
        checkProperty("TZ", TZInfo.local());
        checkProperty("UIScheme", "legacy");
        checkProperty("IMHandle", "LJK");
        _p.setLastLogin(null);
        _p.setLastLogoff(null);
        _p.setHomeAirport(null);
        assertNull(_p.getHomeAirport());
    }
    
    public void testRoles() {
        assertNotNull(_p.getRoles());
        assertEquals(0, _p.getRoles().size());
        _p.addRole("TESTROLE");
        assertEquals(1, _p.getRoles().size());
        assertTrue(_p.isInRole("TESTROLE"));
        List<String> roles = new ArrayList<String>(_p.getRoles());
        assertEquals("TESTROLE", roles.get(0));
    }
    
    public void testNetworkIDs() {
        assertFalse(_p.hasNetworkID(OnlineNetwork.VATSIM));
        _p.setNetworkID(OnlineNetwork.VATSIM, "12345");
        assertNotNull(_p.getNetworkID(OnlineNetwork.VATSIM));
        assertEquals("12345", _p.getNetworkID(OnlineNetwork.VATSIM));
        try {
            _p.setNetworkID(null, "ID");
            fail("NullPointerException expected");
        } catch (NullPointerException npe) {
            return;
        }
    }
    
    public void testNotifyOptions() {
    	assertNotNull(_p.getNotifyOptions());
    	assertEquals(0, _p.getNotifyOptions().size());
        assertFalse(_p.getNotifyOption("EVENT"));
        _p.setNotifyOption("EVENT", true);
        assertTrue(_p.getNotifyOption("EVENT"));
        _p.setNotifyOption("EVENT", false);
        assertFalse(_p.getNotifyOption("EVENT"));
        try {
            _p.setNotifyOption(null, true);
            fail("NullPointerException expected");
        } catch (NullPointerException npe) { }
        
        assertEquals(1, _p.getNotifyOptions().size());
    }
    
    public void testValidation() {
        _p.setCreatedOn(new Date());
        validateInput("lastLogin", new Date(_p.getCreatedOn().getTime() + 15000), IllegalArgumentException.class);
        validateInput("lastLogin", new Date(_p.getCreatedOn().getTime() - 1), IllegalStateException.class);
        validateInput("lastLogoff", new Date(_p.getCreatedOn().getTime() + 15000), IllegalArgumentException.class);
        validateInput("lastLogoff", new Date(_p.getCreatedOn().getTime() - 1), IllegalStateException.class);
        validateInput("loginCount", new Integer(-1), IllegalArgumentException.class);
        validateInput("ID", new Integer(-1), IllegalArgumentException.class);
        validateInput("airportCodeType", "XXX", IllegalArgumentException.class);
        validateInput("airportCodeType", new Integer(-1), IllegalArgumentException.class);
        validateInput("airportCodeType", new Integer(31), IllegalArgumentException.class);
        validateInput("emailAccess", new Integer(-1), IllegalArgumentException.class);
        validateInput("emailAccess", new Integer(11), IllegalArgumentException.class);
        validateInput("status", new Integer(-1), IllegalArgumentException.class);
        validateInput("legacyHours", new Double(-1), IllegalArgumentException.class);
    }
}