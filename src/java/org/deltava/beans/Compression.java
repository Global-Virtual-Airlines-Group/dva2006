// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.io.*;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStream;

import org.deltava.util.BZip2MultiInputStream;

/**
 * An enumeration of file compression types.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public enum Compression {
	NONE, GZIP, BZIP2, BROTLI;
	
	/**
	 * Returns the compression type for a given file name.
	 * @param fileName the file name
	 * @return a CompressionType
	 */
	public static Compression get(String fileName) {
		String fn = fileName.toLowerCase();
		if (fn.endsWith("bz2"))
			return BZIP2;
		else if (fn.endsWith(".gz"))
			return GZIP;
		else if (fn.endsWith(".br"))
			return BROTLI;
		
		return NONE;
	}
	
	/**
	 * Returns a compressed input stream for a particular compression type.
	 * @param is the raw InputStream
	 * @return a decompressor InputStream for this compression type
	 * @throws IOException if an I/O error occurs
	 */
	public InputStream getCompressedStream(InputStream is) throws IOException {
		return switch (this) {
			case GZIP -> new GZIPInputStream(is, 16384);
			case BZIP2 -> new BZip2MultiInputStream(is);
			case BROTLI -> new BrotliCompressorInputStream(is); 
			default -> is;
		};
	}
}