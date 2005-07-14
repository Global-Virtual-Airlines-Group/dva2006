package org.deltava.util.system;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TagTestSystemDataLoader implements SystemDataLoader {

    public TagTestSystemDataLoader() {
        super();
    }

    public Map load() throws IOException {
        Map results = new HashMap();
        results.put("airline.name", "Airline Name");
        results.put("path.css", "/css");
        results.put("path.js", "/jslib");
        results.put("path.img", "/imgs");
        results.put("html.table.spacing", "3");
        results.put("html.table.padding", "4");
        return results;
    }

    public void save(Map properties) throws IOException {
        throw new UnsupportedOperationException();
    }
}