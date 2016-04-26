package org.deltava.service;

import junit.framework.Test;
import junit.framework.TestCase;
import org.hansel.CoverageDecorator;

@SuppressWarnings("static-method")
public class TestServiceException extends TestCase {
    
    public static Test suite() {
        return new CoverageDecorator(TestServiceException.class, new Class[] { ServiceException.class } );
    }

	public void testMessage() {
        try {
            throw new ServiceException(1, "MSG");
        } catch (ServiceException se) {
            assertEquals("MSG", se.getMessage());
            assertEquals(1, se.getCode());
            assertFalse(se.getLogStackDump());
        }
    }
    
    public void testCause() {
        Exception e = new NullPointerException();
        
        try {
            throw new ServiceException(1, "MSG", e);
        } catch (ServiceException se) {
            assertEquals("MSG", se.getMessage());
            assertEquals(e, se.getCause());
            assertEquals(1, se.getCode());
            assertFalse(se.getLogStackDump());
        }
    }
    
    public void testLogStackDump() {
    	ServiceException se = new ServiceException(1, "MSG");
    	assertFalse(se.getLogStackDump());
    	se.setLogStackDump(true);
    	assertTrue(se.getLogStackDump());
    }
}