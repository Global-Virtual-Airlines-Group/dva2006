package org.deltava.beans.schedule;

import java.io.*;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

public class TestChart extends AbstractBeanTestCase {
    
    private Chart _c;
    
    public static Test suite() {
        return new CoverageDecorator(TestChart.class, new Class[] { Chart.class });
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        Airport a = new Airport("ATL", "KATL", "Atlanta GA");
        _c = new Chart("MACEY TWO ARRIVAL", a);
        setBean(_c);
    }
    
    protected void tearDown() throws Exception {
        _c = null;
        super.tearDown();
    }

    public void testProperties() throws IOException {
        assertEquals("ATL", _c.getAirport().getIATA());
        assertEquals("MACEY TWO ARRIVAL", _c.getName());
        checkProperty("ID", new Integer(123));
        checkProperty("size", new Integer(123400));
        checkProperty("type", new Integer(5));
        assertEquals(Chart.TYPENAMES[5], _c.getTypeName());
        checkProperty("imgType", new Integer(1));
        assertEquals(Chart.IMG_TYPE[1].toUpperCase(), _c.getImgTypeName());
        _c.setType(Chart.TYPES[Chart.GROUND]);
        assertEquals(Chart.GROUND, _c.getType());
        
        InputStream imgS = _c.getInputStream();
        assertNotNull(imgS);
        assertEquals(0, imgS.available());
    }
    
    public void testComboAlias() {
    	_c.setID(123);
    	assertEquals(_c.getName(), _c.getComboName());
    	assertEquals("0x" + Integer.toHexString(_c.getID()), _c.getComboAlias());
    }
    
    public void testComparison() {
        Chart c2 = new Chart("MACEY TWO DEPARTURE", new Airport("ATL", "KATL", "Atlanta GA"));
        Chart c3 = new Chart("MACEY TWO DEPARTURE", new Airport("ABQ", "KABQ", "Albequerque NM"));
        assertTrue((_c.compareTo(c2) < 0));
        assertTrue((c2.compareTo(_c) > 0));
        assertTrue((c2.compareTo(c3) > 0));
        assertEquals(0, _c.compareTo(_c));
        assertTrue(_c.equals(_c));
        assertFalse(_c.equals(c3));
        assertFalse(_c.equals(new Object()));
    }
    
    public void testValidation() {
        validateInput("size", new Integer(0), IllegalArgumentException.class);
        validateInput("type", new Integer(-1), IllegalArgumentException.class);
        validateInput("type", new Integer(10), IllegalArgumentException.class);
        validateInput("imgType", new Integer(-1), IllegalArgumentException.class);
        validateInput("imgType", new Integer(3), IllegalArgumentException.class);
        validateInput("type", "X", IllegalArgumentException.class);
        validateInput("type", null, IllegalArgumentException.class);
        try {
            Chart c2 = new Chart(null, new Airport("ATL", "KATL", "Atlanta GA"));
            fail("NullPointerException expected");
            assertNull(c2);
        } catch (NullPointerException npe) { 
        	// empty
        }
    }
    
    public void testBlobBuffer() throws IOException {
        File f = new File("data/testImage.jpg");
        assertTrue(f.exists());
        InputStream is = new FileInputStream(f);
        _c.load(is);
        is.close();

        InputStream imgS = _c.getInputStream();
        assertNotNull(imgS);
        assertEquals(_c.getSize(), imgS.available());
    }
}