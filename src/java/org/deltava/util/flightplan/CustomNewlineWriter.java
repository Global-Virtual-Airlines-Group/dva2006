// Copyright 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.flightplan;

import java.io.*;

/**
 * A PrintWriter to use custom linefeed characters. 
 * @author Luke
 * @version 2.8
 * @since 2.4
 */

class CustomNewlineWriter extends PrintWriter {
	
	private String _newLine;

	/**
	 * Initializes the Writer.
	 * @param out the output stream to write to
	 * @param newLine the line break string
	 */
	CustomNewlineWriter(OutputStream out, String newLine) {
		super(out);
		_newLine = newLine;
	}

	/**
	 * Writes a new line using the specified line break string.
	 */
	@Override
	public void println() {
		print(_newLine);
	}
}