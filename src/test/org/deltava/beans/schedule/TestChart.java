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
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        Airport a = new Airport("ATL", "KATL", "Atlanta GA");
        _c = new Chart("MACEY TWO ARRIVAL", a);
        setBean(_c);
    }
    
    @Override
	protected void tearDown() throws Exception {
        _c = null;
        super.tearDown();
    }

    public void testProperties() throws IOException {
        assertEquals("ATL", _c.getAirport().getIATA());
        assertEquals("MACEY TWO ARRIVAL", _c.getName());
        checkProperty("ID", Integer.valueOf(123));
        checkProperty("size", Integer.valueOf(123400));
        _c.setType(Chart.Type.GROUND);
        assertEquals(Chart.Type.GROUND, _c.getType());
        
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
        validateInput("size", Integer.valueOf(0), IllegalArgumentException.class);
        validateInput("type", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("type", Integer.valueOf(10), IllegalArgumentException.class);
        validateInput("imgType", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("imgType", Integer.valueOf(3), IllegalArgumentException.class);
        validateInput("type", "X", IllegalArgumentException.class);
        validateInput("type", null, IllegalArgumentException.class);
        try {
            Chart c2 = new Chart(null, new Airport("ATL", "KATL", "Atlanta GA"));
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