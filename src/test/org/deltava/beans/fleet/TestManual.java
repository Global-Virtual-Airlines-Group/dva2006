package org.deltava.beans.fleet;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import java.io.File;

import org.deltava.beans.AbstractBeanTestCase;

public class TestManual extends AbstractBeanTestCase {

    private Manual _m;
    
    public static Test suite() {
        return new CoverageDecorator(TestManual.class, new Class[] { Manual.class });
    }
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _m = new Manual(new File("data/test.pdf"));
        setBean(_m);
    }

    @Override
	protected void tearDown() throws Exception {
        _m = null;
        super.tearDown();
    }

    public void testVersion() {
        _m.setVersion(2);
        assertEquals(2, _m.getMajorVersion());
        assertEquals(0, _m.getMinorVersion());
        assertEquals(0, _m.getSubVersion());
        assertEquals("2", _m.getVersion());
    }
    
    public void testMinorSub() {
        _m.setVersion(2, 3, 4);
        assertEquals(2, _m.getMajorVersion());
        assertEquals(0, _m.getMinorVersion());
        assertEquals(0, _m.getSubVersion());
        assertEquals("2", _m.getVersion());
    }
}