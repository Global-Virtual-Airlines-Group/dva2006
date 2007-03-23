package org.deltava.mail;

import junit.framework.TestCase;

public class TestMessageContext extends TestCase {

    private MessageContext _ctxt;
    
    protected final class ContextObject {
        
        public static final int INT_FIELD = 1;
        
        public String getClassName() {
            return getClass().getName();
        }
        
        public Object barf() {
            throw new UnsupportedOperationException();
        }
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        _ctxt = new MessageContext();
    }

    protected void tearDown() throws Exception {
        _ctxt = null;
        super.tearDown();
    }
    
    public void testObject() {
        _ctxt.addData("name", "ContextName");
        assertTrue(_ctxt.hasData("name"));
        assertEquals("ContextName", _ctxt.execute("name"));
        assertEquals("ContextName", _ctxt.execute("name.toString"));
        assertEquals("", _ctxt.execute("invalidAttribute"));
        assertEquals("", _ctxt.execute("name.invalidMethod"));
        
        _ctxt.addData("obj", new ContextObject());
        assertTrue(_ctxt.hasData("obj"));
        assertEquals("1", _ctxt.execute("obj.INT_FIELD"));
        assertEquals("", _ctxt.execute("obj.barf"));
    }

    public void testNestedObject() {
        _ctxt.addData("name", "ContextName");
        assertTrue(_ctxt.hasData("name"));
        assertEquals("ContextName", _ctxt.execute("name"));
        assertEquals("java.lang.String", _ctxt.execute("name.getClass.getName"));
    }
}