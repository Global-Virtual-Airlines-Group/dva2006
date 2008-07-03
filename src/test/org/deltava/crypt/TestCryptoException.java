package org.deltava.crypt;

import junit.framework.Test;
import junit.framework.TestCase;
import org.hansel.CoverageDecorator;

public class TestCryptoException extends TestCase {
    
    public static Test suite() {
        return new CoverageDecorator(TestCryptoException.class, new Class[] { CryptoException.class } );
    }    
    
    public void testCause() {
        Exception e = new NullPointerException();
        
        try {
            throw new CryptoException("MSG", e);
        } catch (CryptoException ce) {
            assertEquals("MSG - " + e.getClass().getName(), ce.getMessage());
            assertEquals(e, ce.getCause());
        }
    }
}