// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.event;

import java.nio.charset.StandardCharsets;

import org.deltava.beans.*;

import org.deltava.util.PDFUtils;

/**
 * A bean to store an Online Event briefing.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class Briefing extends DatabaseBlobBean {

	/**
	 * Creates the bean.
	 * @param data the briefing data
	 */
	public Briefing(byte[] data) {
		super();
		load(data);
	}
	
	/**
	 * Returns the file buffer.
	 * @return the buffer
	 * @throws IllegalStateException if not loaded
	 */
	public byte[] getBuffer() {
		if (!isLoaded())
			throw new IllegalStateException("Not loaded");
		
		return _buffer;
	}

	/**
	 * Returns if the briefing format is PDF.
	 * @return TRUE if PDF, otherwise FALSE
	 */
	public boolean getIsPDF() {
		return PDFUtils.isPDF(_buffer);
	}
	
	@Override
	public String toString() {
		return getIsPDF() ? "PDF" : new String(_buffer, StandardCharsets.UTF_8);
	}
}