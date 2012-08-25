// Copyright 2008, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.io.File;

/**
 * A utility class to create a cacheable File.
 * @author Luke
 * @version 5.0
 * @since 2.2
 */

public class CacheableFile extends File implements Cacheable {
	
	private final Object _key;
	
	/**
	 * Creates a cache entry for a File.
	 * @param f the File
	 */
	public CacheableFile(File f) {
		this(f.getPath(), f);
	}
	
	/**
	 * Creates a cache entry for a File with a specific key.
	 * @param key the key
	 * @param f the File
	 */
	public CacheableFile(Object key, File f) {
		super(f.getPath());
		_key = key;
	}

	@Override
	public Object cacheKey() {
		return (_key == null) ? super.getPath() : _key;
	}
}