// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.io.*;

import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

/**
 * A stream to handle multi-stream BZip2 input streams. 
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class BZip2MultiInputStream extends CompressorInputStream {
	
	private final InputStream _in;
	private BZip2CompressorInputStream _bz;

	/**
	 * Creates the stream.
	 * @param in the InputStream
	 * @throws IOException if an I/O error occurs
	 */
	public BZip2MultiInputStream(InputStream in) throws IOException {
		super();
		_in = in;
		_bz = new BZip2CompressorInputStream(_in);
	}

	@Override
	public int read() throws IOException {
		
		// If this is a multistream file, there will be more data that follows that is a valid compressor input stream. Restart the decompressor engine on the new segment of the data.
		int ch = _bz.read();
		if ((ch == -1) && (_in.available() > 0))  {
			// Make use of the fact that if we hit EOF, the data for the old compressor was deleted already, so we don't need to close.
			_bz = new BZip2CompressorInputStream(_in);
			ch = _bz.read();
		}
		
		return ch;
	}
	
	/**
	 * Read the data from read(). This makes sure we funnel through read so we can do our multistream magic.
	 */
	@Override
	public int read(byte[] dest, int off, int len) throws IOException {
		
		if ((off < 0) || (len < 0) || (off + len > dest.length))
			throw new IndexOutOfBoundsException();
		
		int i = 1; int ofs = off;
		int c = read();
		if (c == -1) return -1;
		dest[ofs++] = (byte)c;
		while (i < len) {
			c = read();
			if (c == -1) break;
			dest[ofs++] = (byte)c;
			++i;
		}
		
		return i;
	}
	
	@Override
	public void close() throws IOException {
		_bz.close();
		_in.close();
	}
}