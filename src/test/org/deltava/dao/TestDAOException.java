package org.deltava.dao;

import junit.framework.Test;
import junit.framework.TestCase;

import org.hansel.CoverageDecorator;

public class TestDAOException extends TestCase {
    
    public static Test suite() {
        return new CoverageDecorator(TestDAOException.class, new Class[] { DAOException.class } );
    }

    @SuppressWarnings("static-method")
	public void testMessage() {
        try {
            throw new DAOException("MSG");
        } catch (DAOException de) {
            assertEquals("MSG", de.getMessage());
        }
    }
    
    @SuppressWarnings("static-method")
	public void testCause() {
        Exception e = new NullPointerException();
        try {
            throw new DAOException(e);
        } catch (DAOException de) {
            assertEquals(e.getMessage(), de.getMessage());
            assertEquals(e, de.getCause());
        }
    }
}