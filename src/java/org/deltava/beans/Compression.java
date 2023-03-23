// Copyright 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.io.*;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStream;

import org.deltava.util.BZip2MultiInputStream;

/**
 * An enumeration of file compression types.
 * @author Luke
 * @version 10.5
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
	public InputStream getStream(InputStream is) throws IOException {
		return switch (this) {
			case GZIP -> new GZIPInputStream(is, 16384);
			case BZIP2 -> new BZip2MultiInputStream(is);
			case BROTLI -> new BrotliCompressorInputStream(is); 
			default -> is;
		};
	}
	
	/**
	 * Detects the compression format of a File. Note that there is no standardized magic number for Brotli compressed streams. 
	 * @param f a File
	 * @return a Compression type
	 * @throws IOException if an I/O error occurs
	 */
	public static Compression detect(File f) throws IOException {
		try (InputStream is = new FileInputStream(f)) {
			int fw = is.read() + ((is.read() << 8) & 0xFF00);
			return switch (fw) {
				case GZIPInputStream.GZIP_MAGIC -> GZIP;
				case 0x5A42 -> BZIP2;
				default -> NONE;
			};
		}
	}
}