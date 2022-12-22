// Copyright 2021, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

/**
 * An enumeration of HTTP compression types.
 * @author Luke
 * @version 10.3
 * @since 10.1
 */

public enum Compression {
	NONE, GZIP, COMPRESS, DEFLATE, BROTLI;
	
	/**
	 * Returns the value for the accept-encoding header.
	 * @return the header value
	 */
	public String getEncoding() {
		return switch (this) {
		case NONE -> "*";
		case BROTLI -> "br";
		default -> name().toLowerCase();
		};
	}
	
	/**
	 * Determines compression type from a Content-Encoding header value.
	 * @param hdr the header value
	 * @return a Compression
	 */
	static Compression fromHeader(String hdr) {
		for (int x = 0; x < values().length; x++) {
			Compression c = values()[x];
			if (c.getEncoding().equalsIgnoreCase(hdr))
				return c;
		}
		
		return Compression.NONE;
	}
}