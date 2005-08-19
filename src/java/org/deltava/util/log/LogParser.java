// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.util.log;

import java.io.File;

import org.deltava.beans.stats.HTTPStatistics;

/**
 * An interface to mark HTTP server log processors. Depending on the format of the server log, a
 * custom LogParser will need to be written.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface LogParser {
   
   public HTTPStatistics parseLog(File f);
}