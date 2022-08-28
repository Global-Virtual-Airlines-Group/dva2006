// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.stats;

/**
 * An abstract log book export class to generate CSV-formatted log books.  
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

abstract class CSVExport extends LogbookExport {

	private final StringBuilder _buf = new StringBuilder(); 
	
	/**
	 * Creates the exporter.
	 * @param hdr the CSV header
	 */
	protected CSVExport(String hdr) {
		super();
		writeln(hdr);
	}

	/**
	 * Writes a string to the output buffer, terminated with a newline.
	 * @param data the data
	 */
	protected void writeln(CharSequence data) {
		_buf.append(data);
		_buf.append("\r\n");
	}
	
	@Override
	public final String getContentType() {
		return "text/csv";
	}
	
	@Override
	public final String getExtension() {
		return "csv";
	}
	
	@Override
	public final String toString() {
		return _buf.toString();
	}
}