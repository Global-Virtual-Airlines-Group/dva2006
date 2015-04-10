// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.io.*;

/**
 * A class to make raw data cacheable.
 * @author Luke
 * @version 6.0
 * @since 6.0
 */

public class CacheableBlob implements Cacheable {
	
	private final Object _key;
	private final byte[] _data;

	/**
	 * Creates the object.
	 * @param key the cache key
	 * @param data the data
	 */
	public CacheableBlob(Object key, byte[] data) {
		super();
		_key = key;
		_data = data;
	}
	
	/**
	 * Returns the data.
	 * @return the data
	 */
	public byte[] getData() {
		return _data;
	}

	/**
	 * Returns a stream to the data.
	 * @return the data
	 */
	public InputStream getStream() {
		return new ByteArrayInputStream(_data);
	}

	@Override
	public Object cacheKey() {
		return _key;
	}
}