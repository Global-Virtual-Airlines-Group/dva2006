package org.deltava.beans.system;

import junit.framework.Test;

import org.deltava.beans.AbstractBeanTestCase;
import org.hansel.CoverageDecorator;

public class TestMessageTemplate extends AbstractBeanTestCase {

    private MessageTemplate _msg;
    
    public static Test suite() {
        return new CoverageDecorator(TestMessageTemplate.class, new Class[] { MessageTemplate.class } );
    }
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _msg = new MessageTemplate("TEST");
        setBean(_msg);
    }

    @Override
	protected void tearDown() throws Exception {
        _msg = null;
        super.tearDown();
    }

    public void testProperties() {
        assertEquals("TEST", _msg.getName());
        checkProperty("subject", "Test Message");
        checkProperty("description", "DESC");
        checkProperty("body", "MSGBODY");
        assertEquals(_msg.getName().hashCode(), _msg.hashCode());
    }
    
    public void testValidation() {
        validateInput("description", null, NullPointerException.class);
    }
    
    public void testEquality() {
        MessageTemplate msg2 = new MessageTemplate("TEST");
        MessageTemplate msg3 = new MessageTemplate("TEST2");
        assertNotSame(_msg, msg2);
        assertNotSame(_msg, msg3);
        assertNotSame(msg2, msg3);
        assertTrue(_msg.equals(msg2));
        assertFalse(_msg.equals(new Object()));
        assertTrue(msg2.compareTo(msg3) < 1);
    }
}