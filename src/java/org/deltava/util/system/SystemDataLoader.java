// Copyright 2004, 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.system;

import java.util.Map;
import java.io.IOException;

/**
 * A class that can populate the SystemData model.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface SystemDataLoader {

	/**
	 * Loads the SystemData properties.
	 * @return a Map of properties
	 * @throws IOException if an I/O error occurs
	 */
    public Map<String, Object> load() throws IOException;
}