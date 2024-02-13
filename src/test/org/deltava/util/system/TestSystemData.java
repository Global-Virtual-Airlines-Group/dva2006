package org.deltava.util.system;

import java.io.File;
import java.util.*;

import org.deltava.beans.schedule.Airline;

import junit.framework.TestCase;

@SuppressWarnings("static-method")
public class TestSystemData extends TestCase {

    @Override
	protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
    }

	public void testDefaultLoader() {
        SystemData.init("org.deltava.util.system.XMLSystemDataLoader", true);
        assertEquals("org.deltava.util.system.XMLSystemDataLoader", SystemData.get(SystemData.LOADER_NAME));
        assertEquals(3072000, SystemData.getInt("testing.max_video_size"));
        Map<?, ?> jdbcProps = (Map<?, ?>) SystemData.getObject("jdbc.connectProperties");
        assertNotNull(jdbcProps);
        assertTrue(jdbcProps.containsKey("useNewIO"));
    }
    
    public void testCustomLoader() {
        SystemData.init(MockSystemDataLoader.class.getName(), true);
        assertEquals("test", SystemData.get(SystemData.CFG_NAME));
        assertEquals("STRING", SystemData.get("stringVar"));
        assertTrue(SystemData.getBoolean("boolVar"));
        assertEquals(43, SystemData.getInt("intVar"));
        assertEquals(23, SystemData.getLong("longVar", 0));
        assertEquals(3.1415926, SystemData.getDouble("doubleVar", 0), 0.0001);
    }
    
    public void testAirlineLookup() {
    	SystemData.init("org.deltava.util.system.XMLSystemDataLoader", true);
    	
    	Airline a = new Airline("DL", "Delta Air Lines");
    	a.setActive(true);
    	a.setICAO("DAL");
    	a.setCodes(List.of("DVA", "DVH"));
    	SystemData.add("airlines", Map.of(a.getCode(), a));
    	
    	assertNotNull(SystemData.getAirline("DL"));
    	assertNotNull(SystemData.getAirline("DVA"));
    	assertNotNull(SystemData.getAirline("DVH"));
    	assertNotNull(SystemData.getAirline("DAL"));
    	assertNull(SystemData.getAirline("foo"));
    }
}