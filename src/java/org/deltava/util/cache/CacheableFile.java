// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.io.File;

/**
 * A utility class to create a cacheable File.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class CacheableFile extends File implements Cacheable {
	
	private Object _key;
	
	public CacheableFile(File f) {
		this(f.getPath(), f);
	}
	
	public CacheableFile(Object key, File f) {
		super(f.getPath());
		_key = key;
	}

	@Override
	public Object cacheKey() {
		return (_key == null) ? super.getPath() : _key;
	}
}