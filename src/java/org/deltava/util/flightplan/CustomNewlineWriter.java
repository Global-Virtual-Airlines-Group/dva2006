// Copyright 2009, 2010, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.flightplan;

import java.io.*;

/**
 * A PrintWriter to use custom linefeed characters. 
 * @author Luke
 * @version 5.0
 * @since 2.4
 */

class CustomNewlineWriter extends PrintWriter {
	
	/**
	 * Initializes the Writer.
	 * @param out the Writer to write to
	 */
	CustomNewlineWriter(Writer out) {
		super(out);
	}

	/**
	 * Writes a new line using the specified line break string.
	 */
	@Override
	public void println() {
		print("\r\n");
	}
}