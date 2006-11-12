package org.deltava.util.system;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TagTestSystemDataLoader implements SystemDataLoader {

    public Map<String, Object> load() throws IOException {
        Map<String, Object> results = new HashMap<String, Object>();
        results.put("airline.name", "Airline Name");
        results.put("path.css", "/css");
        results.put("path.js", "/jslib");
        results.put("path.img", "/imgs");
        results.put("html.table.spacing", "3");
        results.put("html.table.padding", "4");
        return results;
    }
}