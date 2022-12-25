// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.util.function.IntConsumer;

/**
 * An InputStream that counts the number of bytes read from it. 
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

class CountingInputStream extends FilterInputStream {
	
	private final IntConsumer _onClose;
	private int _bytesRead;

	/**
	 * Creates the InputStream.
	 * @param in the InputStream to wrap
	 * @param onClose statistics update hook, called on close()
	 */
	CountingInputStream(InputStream in, IntConsumer onClose) {
		super(in);
		_onClose = onClose;
	}
	
	/**
	 * Returns the number of bytes read from this stream.
	 * @return the number of bytes
	 */
	public int getCount() {
		return _bytesRead;
	}

	@Override
	public int read() throws IOException {
		int ch = super.read();
		_bytesRead++;
		return ch;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int cnt = super.read(b, off, len);
		_bytesRead += cnt;
		return cnt;
	}
	
	@Override
	public void close() throws IOException {
		if (_onClose != null)
			_onClose.accept(_bytesRead);
		
		super.close();
	}
}