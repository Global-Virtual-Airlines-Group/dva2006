package org.deltava.jdbc;

import junit.framework.Test;
import junit.framework.TestCase;

import org.hansel.CoverageDecorator;

public class TestConnectionPoolException extends TestCase {
    
    public static Test suite() {
        return new CoverageDecorator(TestConnectionPoolException.class, new Class[] { ConnectionPoolException.class } );
    }

    public void testMessage() {
        try {
            throw new ConnectionPoolException("MSG", false);
        } catch (ConnectionPoolException cpe) {
            assertEquals("MSG", cpe.getMessage());
        }
    }
    
    public void testCause() {
        Exception e = new NullPointerException();
        
        try {
            throw new ConnectionPoolException(e);
        } catch (ConnectionPoolException cpe) {
            assertEquals(e.getMessage(), cpe.getMessage());
            assertEquals(e, cpe.getCause());
        }
    }
}