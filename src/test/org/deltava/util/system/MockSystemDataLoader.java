package org.deltava.util.system;

import java.util.HashMap;
import java.util.Map;

public class MockSystemDataLoader implements SystemDataLoader {
    
    @Override
	public Map<String, Object> load() {
        Map<String, Object> results = new HashMap<String, Object>();
        results.put(SystemData.LOADER_NAME, getClass().getName());
        results.put(SystemData.CFG_NAME, "test");
        results.put("stringVar", "STRING");
        results.put("boolVar", Boolean.valueOf(true));
        results.put("intVar", Integer.valueOf(43));
        results.put("longVar", Long.valueOf(23));
        results.put("doubleVar", Double.valueOf(3.1415926));
        results.put("airline.code", "DVA");
        return results;
    }
}