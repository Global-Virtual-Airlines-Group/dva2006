package org.deltava.beans;

import java.time.Instant;
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
        
        @Override
		public String getStatusName() {
        	return "MockPerson";
        }
        
        @Override
		public final void setStatus(int status) {
            super.setStatus(status);
        }
        
        @Override
		public void addRole(String roleName) {
            _roles.add(roleName);
        }
        
        @Override
		public Collection<String> getRoles() {
            return _roles;
        }
        
        @Override
		public boolean isInRole(String roleName) {
            return _roles.contains(roleName);
        }
        
        @Override
		public String getRowClassName() {
        	return null;
        }
    }
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _p = new MockPerson("John", "Smith");
        setBean(_p);
    }
    
    @Override
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
        _p.setAirportCodeType(Airport.Code.IATA);
        assertEquals(0, _p.getAirportCodeType().ordinal());
        checkProperty("emailAccess", Integer.valueOf(1));
        checkProperty("legacyHours", new Double(238.1));
        checkProperty("createdOn", Instant.now());
        checkProperty("lastLogin", Instant.now());
        checkProperty("lastLogoff", Instant.now());
        checkProperty("loginCount", Integer.valueOf(12));
        checkProperty("loginHost", "localhost");
        checkProperty("status", Integer.valueOf(2));
        checkProperty("dateFormat", "MM/dd/yyyy");
        checkProperty("timeFormat", "hh:mm:ss");
        checkProperty("numberFormat", "#,##0.0");
        checkProperty("TZ", TZInfo.UTC);
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
        assertFalse(_p.hasNotifyOption(Notification.EVENT));
        _p.setNotifyOption(Notification.EVENT, true);
        assertTrue(_p.hasNotifyOption(Notification.EVENT));
        _p.setNotifyOption(Notification.EVENT, false);
        assertFalse(_p.hasNotifyOption(Notification.EVENT));
        try {
            _p.setNotifyOption(null, true);
            fail("NullPointerException expected");
        } catch (NullPointerException npe) {
        	// empty
        }
        
        assertEquals(1, _p.getNotifyOptions().size());
    }
    
    public void testValidation() {
        _p.setCreatedOn(Instant.now());
        validateInput("lastLogin", _p.getCreatedOn().plusSeconds(15), IllegalArgumentException.class);
        validateInput("lastLogin", _p.getCreatedOn().minusSeconds(1), IllegalStateException.class);
        validateInput("lastLogoff", _p.getCreatedOn().plusSeconds(15), IllegalArgumentException.class);
        validateInput("lastLogoff", _p.getCreatedOn().minusSeconds(1), IllegalStateException.class);
        validateInput("loginCount", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("ID", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("airportCodeType", "XXX", IllegalArgumentException.class);
        validateInput("airportCodeType", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("airportCodeType", Integer.valueOf(31), IllegalArgumentException.class);
        validateInput("emailAccess", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("emailAccess", Integer.valueOf(11), IllegalArgumentException.class);
        validateInput("status", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("legacyHours", new Double(-1), IllegalArgumentException.class);
    }
}