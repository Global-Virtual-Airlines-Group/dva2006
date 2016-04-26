package org.deltava.beans.stats;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

public class TestTableInfo extends AbstractBeanTestCase {

    private TableInfo _info;
    
    public static Test suite() {
        return new CoverageDecorator(TestTableInfo.class, new Class[] { TableInfo.class } );
    }

    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _info = new TableInfo("TABLE");
        setBean(_info);
    }

    @Override
	protected void tearDown() throws Exception {
        _info = null;
        super.tearDown();
    }

    public void testProperties() {
        assertEquals("TABLE", _info.getName());
        checkProperty("size", new Long(123400));
        assertEquals(0, _info.getRows());
        assertEquals(0, _info.getAverageRowLength());
        checkProperty("rows", Integer.valueOf(1234));
        checkProperty("indexSize", new Long(125632));
        assertEquals(100, _info.getAverageRowLength());
        assertEquals(_info.getName().hashCode(), _info.hashCode());
    }
    
    public void testValidation() {
        validateInput("rows", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("size", new Long(-1), IllegalArgumentException.class);
        validateInput("indexSize", new Long(-1), IllegalArgumentException.class);
    }
    
    public void testComparator() {
       TableInfo ti2 = new TableInfo("TABLE2");
       TableInfo ti3 = new TableInfo("TABLE");
       assertNotSame(_info, ti2);
       assertNotSame(_info, ti3);
       assertTrue(_info.compareTo(ti2) < 0);
       assertTrue(ti2.compareTo(ti3) > 0);
       assertTrue(_info.equals(ti3));
       assertFalse(_info.equals(new Object()));
       assertFalse(_info.equals(null));
       assertFalse(_info.equals(ti2));
    }
}