// Copyright 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.io.*;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.compressors.brotli.BrotliCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

import org.deltava.util.BZip2MultiInputStream;

/**
 * An enumeration of file compression types.
 * @author Luke
 * @version 10.6
 * @since 10.3
 */

public enum Compression {
	NONE, GZIP, BZIP2, BROTLI, XZ;
	
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
		else if (fn.endsWith(".xz"))
			return XZ;
		
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
			case XZ -> new XZCompressorInputStream(is);
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
		try (DataInputStream dis = new DataInputStream(new FileInputStream(f))) {
			int fw = dis.readShort() & 0xFFFF; // clear top 16 bits, need int since XZ header is negative
			int ni = dis.readInt();
			return switch (fw) {
				case 0x1F8B -> GZIP;
				case 0x425A -> BZIP2;
				case 0xFD37 -> (ni == 0x7A585A00) ? XZ : NONE;
				default -> NONE;
			};
		}
	}
}