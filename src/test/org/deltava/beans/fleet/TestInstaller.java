package org.deltava.beans.fleet;

import java.io.File;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

public class TestInstaller extends AbstractBeanTestCase {

    private Installer _i;
    
    public static Test suite() {
        return new CoverageDecorator(TestInstaller.class, new Class[] { FleetEntry.class, Installer.class });
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        _i = new Installer("data/users.txt");
        setBean(_i);
    }

    protected void tearDown() throws Exception {
        _i = null;
        super.tearDown();
    }
    
    public void testProperties() {
        File f = new File("data/users.txt");
        assertEquals("users.txt", _i.getFileName());
        assertEquals(f.getAbsolutePath(), _i.getFullName());
        assertEquals(f.length(), _i.getSize());
        assertNull(_i.getRowClassName());
        
        _i.setCode(null);
        assertNull(_i.getCode());

        checkProperty("image", "dc8.gif");
        checkProperty("name", "DC-8 Installer");
        checkProperty("description", "The DC-8 Installer");
        checkProperty("code", "DC8");
        checkProperty("downloadCount", new Integer(3));
        checkProperty("security", new Integer(1));
        
        assertEquals(_i.getName(), _i.cacheKey());
        assertEquals(_i.getName().hashCode(), _i.hashCode());
        
        _i.setVersion(1, 2, 3);
        assertEquals(1, _i.getMajorVersion());
        assertEquals(2, _i.getMinorVersion());
        assertEquals(3, _i.getSubVersion());
        assertEquals("1.2.3", _i.getVersion());
        assertEquals("123", _i.getVersionCode());
    }
    
    public void testValidation() {
       validateInput("size", new Long(120400), IllegalStateException.class);
        validateInput("description", null, NullPointerException.class);
        validateInput("security", new Integer(-1), IllegalArgumentException.class);
        validateInput("security", new Integer(31), IllegalArgumentException.class);
        validateInput("security", "XXX", IllegalArgumentException.class);
        validateInput("downloadCount", new Integer(-1), IllegalArgumentException.class);
        try {
            _i.setVersion(-1, 1, 2);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException iae) {
        	// empty
        }
        
        try {
            _i.setVersion(1, -1, 2);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException iae) {
        	// empty
        }
        
        try {
            _i.setVersion(1, 1, -2);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException iae) { 
        	// empty
        }
    }
    
    public void testComboAlias() {
       _i.setName("Installer");
       assertEquals(_i.getName(), _i.getComboName());
       assertEquals(_i.getFileName(), _i.getComboAlias());
    }
    
    public void testComparator() {
       _i.setName("Installer");
       Installer i2 = new Installer("data/dummyFile.txt");
       i2.setName("H Installer");
       
       assertTrue(_i.compareTo(i2) > 0);
       assertTrue(i2.compareTo(_i) < 0);
    }
    
    public void testInvalidFile() {
        File f = new File("data/dummyFile.txt");
        assertFalse(f.exists());
        Installer i2 = new Installer("data/dummyFile.txt");
        assertEquals("dummyFile.txt", i2.getFileName());
        assertEquals(0, i2.getSize());
        assertEquals("warn", i2.getRowClassName());
        
        setBean(i2);
        validateInput("size", new Long(-1), IllegalArgumentException.class);
        checkProperty("size", new Long(102400));
    }
}