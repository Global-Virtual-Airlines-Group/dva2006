// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.util.function.IntConsumer;

/**
 * An OutputStream that counts the number of bytes written to it.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class CountingOutputStream extends FilterOutputStream {
	
	private final IntConsumer _onClose;
	private int _bytesWritten;

	/**
	 * Creates the OutputStream.
	 * @param out the OutputStream to wrap
	 * @param onClose statistics update hook, called on close()
	 */
	public CountingOutputStream(OutputStream out, IntConsumer onClose) {
		super(out);
		_onClose = onClose;
	}

	/**
	 * Returns the number of bytes written to this stream.
	 * @return the number of bytes
	 */
	public int getCount() {
		return _bytesWritten;
	}
	
	@Override
	public void write(int b) throws IOException {
		super.write(b);
		_bytesWritten++;
	}
	
	@Override
	public void close() throws IOException {
		if (_onClose != null)
			_onClose.accept(_bytesWritten);
		
		super.close();
	}
}