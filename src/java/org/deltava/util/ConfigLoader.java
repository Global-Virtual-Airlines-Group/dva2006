// Copyright 2005, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.io.*;

/**
 * A utility class to support loading configuration resources on the local filesystem. This class ensures similar
 * behavior when running in a servlet container or within the Eclipse Workbench.
 * @author Luke
 * @version 4.2
 * @since 1.0
 */

public class ConfigLoader {
   
	// singleton
	private ConfigLoader() {
		super();
	}
	
   /**
    * Retrieves a resource as an input stream.
    * @param fName the file name
    * @return an InputStream to the file
    * @throws IOException if the file does not exist
    */
   public static InputStream getStream(String fName) throws IOException {
       InputStream is = ConfigLoader.class.getResourceAsStream(fName);
       if (is == null)
			is = new FileInputStream(fName.substring(1));

       return is;
   }
}