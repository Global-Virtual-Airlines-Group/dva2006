// Copyright 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.nio.charset.StandardCharsets;

import org.deltava.util.PDFUtils;

/**
 * An abstract bean that contains a document. This has helper methods that provide PDF detection and
 * allow the size to be loaded without loading the entire binary payload.
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public abstract class DatabaseDocumentBean extends DatabaseBlobBean {

	private int _size;
	private boolean _isPDF;
	
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
		return isLoaded() ? PDFUtils.isPDF(_buffer) : _isPDF;
	}

	@Override
	public int getSize() {
		return isLoaded() ? super.getSize() : _size;
	}
	
	/**
	 * Returns the document's MIME type.
	 * @return the MIME type
	 */
	public String getContentType() {
		return getIsPDF() ? "application/pdf" : "text/plain";
	}
	
	/**
	 * Returns the document's file extension.
	 * @return the extension
	 */
	public String getExtension() {
		return getIsPDF() ? "pdf" : "txt";
	}
	
	/**
	 * Loads a text document from a string.
	 * @param txt the text
	 */
	public void load(String txt) {
		load(txt.getBytes(StandardCharsets.UTF_8));
	}
	
	/**
	 * Forces the object type.
	 * @param isPDF TRUE if a PDF, otherwise FALSE
	 */
	public void setForcePDF(boolean isPDF) {
		_isPDF = isPDF;
	}
	
	/**
	 * Forces the object size.
	 * @param size the size in bytes
	 */
	public void setForceSize(int size) {
		_size = size;
	}
	
	/**
	 * Returns a text representation of the document. Since some subclasses may not just be a briefing document, this deliberately does not override toString().
	 * @return the briefing text, or &quot;PDF&quot; if a PDF document
	 */
	public String getText() {
		return getIsPDF() ? "PDF" : new String(_buffer, java.nio.charset.StandardCharsets.UTF_8);
	}
}