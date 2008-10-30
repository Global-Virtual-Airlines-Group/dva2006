// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.cache;

import java.io.*;

/**
 * A FileSystemCache is a {@link FileCache} that enforces storage within a particular
 * directory on the filesystem, and can therefore be preloaded from the filesystem
 * on JVM statrup.  
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class FileSystemCache extends FileCache {
	
	private File _location;
	private boolean _isLoaded;
	
	protected class CacheFilter implements FileFilter {
		public boolean accept(File f) {
			return f.isFile() && f.getName().endsWith(".cache");
		}
	}
	
	/**
	 * Helper method to move files.
	 */
	private void move(File f, File f2) {
		int bufferSize = (int) Math.max(65536, f.length());
		byte[] buf = new byte[bufferSize];
		try {
			InputStream in = new FileInputStream(f);
			OutputStream out = new FileOutputStream(f2);
			int bytesRead = 0;
			do {
				bytesRead = in.read(buf);
				out.write(buf, 0, bytesRead);
			} while (bytesRead == bufferSize);
			
			in.close();
			out.close();
		} catch (IOException ie) {
			// empty
		} finally {
			f.delete();
		}
	}

	/**
	 * Initializes the cache.
	 * @param maxSize the maximum number of entries
	 * @param location the location of the files on the file system
	 */
	public FileSystemCache(int maxSize, String location) {
		super(maxSize);
		_location = new File(location);
	}
	
	/**
	 * Returns the cache location.
	 * @return the location
	 */
	public File getLocation() {
		return _location;
	}
	
	/**
	 * Returns if the cache has been pre-loaded.
	 * @return TRUE if pre-loaded, otherwise FALSE
	 * @see FileSystemCache#load()
	 */
	public boolean isLoaded() {
		return _isLoaded;
	}

	/**
	 * Adds an entry to the cache. If the file 
	 * @param obj the file to add to the cache
	 */
	protected final void addEntry(CacheableFile obj) {
		if (obj == null)
			return;
		
		// If we have an existing file, delete it
		if (_cache.containsKey(obj.cacheKey()))
			remove(obj.cacheKey());
		
		// Move the file into the cache
		String sKey = String.valueOf(obj.cacheKey());
		if (!obj.getParentFile().equals(_location)) {
			File newF = new File(_location, sKey + ".cache");
			if (!obj.renameTo(newF))
				move(obj, newF);
			
			obj =  new CacheableFile(sKey, newF);
		}
		
		// Add the entry
		super.addEntry(new CacheableFile(sKey, obj));
	}
	
	/**
	 * Returns an entry from the cache.
	 * @param key the cache key
	 * @return the file, or null if not present
	 */
	public final CacheableFile get(Object key) {
		try {
			load();
		} catch (IOException ie) {
			// empty
		}

		return super.get(String.valueOf(key));
	}
	
	/**
	 * Pre-loads the cache from the file system. 
	 * @throws IOException if an I/O error occurs
	 * @see FileSystemCache#isLoaded()
	 */
	public void load() throws IOException {
		if (_isLoaded)
			return;
		else if (!_location.isDirectory())
			throw new IOException(_location + " is not a directory");

		// Load the files
		File[] entries = _location.listFiles(new CacheFilter());
		for (int x = 0; (entries != null) && (x < entries.length); x++) {
			File f = entries[x];
			Object key = f.getName().substring(0, f.getName().lastIndexOf('.'));
			CacheableFile cf = new CacheableFile(key, f);
			addEntry(cf);
		}
		
		// Mark the cache as loaded
		_isLoaded = true;
	}
}