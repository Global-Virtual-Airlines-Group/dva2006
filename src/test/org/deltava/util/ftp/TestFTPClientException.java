package org.deltava.util.ftp;

import org.hansel.CoverageDecorator;

import junit.framework.Test;
import junit.framework.TestCase;

public class TestFTPClientException extends TestCase {
	
    public static Test suite() {
        return new CoverageDecorator(TestFTPClientException.class, new Class[] { FTPClientException.class } );
    }

    @SuppressWarnings("static-method")
	public void testCause() {
        Exception e = new NullPointerException();
        try {
            throw new FTPClientException(e, true);
        } catch (FTPClientException de) {
            assertEquals(e.getMessage(), de.getMessage());
            assertEquals(e, de.getCause());
            assertTrue(de.getDumpStack());
        }
    }
}