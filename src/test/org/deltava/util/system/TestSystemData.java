package org.deltava.util.system;

import java.util.*;
import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;

public class TestSystemData extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
        PropertyConfigurator.configure("data/log4j.test.properties");
    }

    public void testDefaultLoader() {
        SystemData.init("org.deltava.util.system.XMLSystemDataLoader", true);
        assertEquals("org.deltava.util.system.XMLSystemDataLoader", SystemData.get(SystemData.LOADER_NAME));
        assertEquals(3072000, SystemData.getInt("testing.max_video_size"));
        List eqTypes = (List) SystemData.getObject("eqtypes");
        assertNotNull(eqTypes);
        assertTrue(eqTypes.contains("CRJ-200"));
        Map jdbcProps = (Map) SystemData.getObject("jdbc.connectProperties");
        assertNotNull(jdbcProps);
        assertTrue(jdbcProps.containsKey("useNewIO"));
        
        // Test that ranks are in the proper order
        List ranks = (List) SystemData.getObject("ranks");
        assertNotNull(ranks);
        assertTrue(ranks.indexOf("First Officer") < ranks.indexOf("Captain"));
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
}