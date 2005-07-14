package org.deltava.commands;

/**
 * A class to support HTTP response caching.
 * @author Luke
 * @version 1.0
 * @since 1.0
 * Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
 */

public class CacheControl {
	
	/**
	 * Used to avoid writing any cache control headers to the resposne.
	 */
	public static final int DEFAULT_CACHE = -1;

	private boolean _public;
	private int _maxAge = DEFAULT_CACHE;
	
	/**
	 * Creates a new cache control bean. This is package-private since it is only called by the CommandContext constructor.
	 */
	CacheControl() {
		super();
	}
	
	/**
	 * Returns if the response should be stored in a shared proxy cache.
	 * @return TRUE if the response is public, otherwase FALSE
	 * @see CacheControl#setPublic(boolean)
	 */
	public boolean isPublic() {
		return _public;
	}
	
	/**
	 * Returns the maximum validity of the response.
	 * @return the validity time in seconds
	 * @see CacheControl#setMaxAge(int)
	 */
	public int getMaxAge() {
		return _maxAge;
	}
	
	/**
	 * Marks the response as being suitable for storage in shared proxy caches.
	 * @param isPublic TRUE if the response is public, otherwise FALSE
	 * @see CacheControl#isPublic()
	 */
	public void setPublic(boolean isPublic) {
		_public = isPublic;
	}
	
	/**
	 * Sets the maximum validity of the response.
	 * @param age the validity time in seconds.
	 * @see CacheControl#getMaxAge()
	 */
	public void setMaxAge(int age) {
		_maxAge = (age < DEFAULT_CACHE) ? DEFAULT_CACHE : age;
	}
}