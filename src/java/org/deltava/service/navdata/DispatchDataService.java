// Copyright 2008, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.navdata;

import java.io.*;

import org.deltava.service.*;

import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * An abstract class to store a common cache for Dispatch Web Services.
 * @author Luke
 * @version 4.1
 * @since 2.2
 */

public abstract class DispatchDataService extends DownloadService {
	
	protected static final FileSystemCache _dataCache = new FileSystemCache(16, SystemData.get("schedule.cache"));
	
	/**
	 * Adds a file to the shared file cache. 
	 * @param key the cache key
	 * @param f the file
	 */
	protected void addCacheEntry(Object key, File f) {
		if ((f != null) && f.exists())
			_dataCache.add(new CacheableFile(key, f));
	}
	
	/**
	 * Clears the file cache.
	 * @see FileSystemCache#clear()
	 */
	public static synchronized void invalidate() {
		_dataCache.clear();
	}
}