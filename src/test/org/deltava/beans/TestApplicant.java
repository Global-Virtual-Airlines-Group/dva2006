package org.deltava.beans;

import junit.framework.Test;

import org.hansel.CoverageDecorator;

public class TestApplicant extends AbstractBeanTestCase {
	
	public static Test suite() {
		return new CoverageDecorator(TestApplicant.class, new Class[] { Applicant.class } );
   }
    
    private Applicant _a;
    
    protected void setUp() throws Exception {
        super.setUp();
        _a = new Applicant("John", "Smith");
        setBean(_a);
    }
    
    protected void tearDown() throws Exception {
        _a = null;
        super.tearDown();
    }

    public void testProperties() {
        assertEquals("John", _a.getFirstName());
        assertEquals("Smith", _a.getLastName());
        assertNull(_a.getLegacyURL());
        assertFalse(_a.getLegacyVerified());
        checkProperty("legacyURL", "http://www.deltava.org/");
        checkProperty("legacyVerified", Boolean.valueOf(true));
        checkProperty("registerHostName", "localhost");
        checkProperty("pilotID", new Integer(0));
        
        _a.setStatus(Applicant.APPROVED);
        checkProperty("pilotID", new Integer(2345));
        
        assertNotNull(_a.getRoles());
        assertEquals(1, _a.getRoles().size());
        assertTrue(_a.isInRole("Applicant"));
        assertTrue(_a.isInRole("*"));
        assertFalse(_a.isInRole("Pilot"));
    }
    
    public void testValidation() {
    	validateInput("status", new Integer(-1), IllegalArgumentException.class);
    	validateInput("status", new Integer(121), IllegalArgumentException.class);
    	assertFalse(_a.getStatus() == Applicant.APPROVED);
    	validateInput("pilotID", new Integer(123), IllegalStateException.class);
    	 _a.setStatus(Applicant.APPROVED);
    	validateInput("pilotID", new Integer(-1), IllegalArgumentException.class);
    	
    	try {
    		_a.addRole("any Role");
    		fail("UnsupportedOperationException expected");
    	} catch (UnsupportedOperationException uoe) {
    		// empty
    	}
    }
    
    public void testViewEntry() {
       String[] ROW_CLASSES = {"opt1", null, "err"};
       for (int x = 0; x < ROW_CLASSES.length; x++) {
          _a.setStatus(x);
          assertEquals(ROW_CLASSES[x], _a.getRowClassName());
       }
    }
}