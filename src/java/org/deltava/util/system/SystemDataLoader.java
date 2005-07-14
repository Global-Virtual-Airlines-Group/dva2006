package org.deltava.util.system;

import java.util.Map;
import java.io.IOException;

/**
 * @author Luke
 * @version 1.0
 * @since 1.0
 */
public interface SystemDataLoader {

    public Map load() throws IOException;
    public void save(Map properties) throws IOException;
}