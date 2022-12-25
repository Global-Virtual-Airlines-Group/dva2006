// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;

/**
 * A bean to store HTTP compression statistics. 
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class HTTPCompressionInfo implements java.io.Serializable {
	
	private static final Map<String, HTTPCompressionInfo> _stats = new HashMap<String, HTTPCompressionInfo>();
	
	private final String _id;
	private int _reqs;
	private long _rawBytes;
	private long _totalBytes;

	/**
	 * Restrieves a statistics bean.
	 * @param id the ID
	 * @return an HTTPCompressionInfo bean
	 */
	public static synchronized HTTPCompressionInfo get(String id) {
		return _stats.computeIfAbsent(id, k -> new HTTPCompressionInfo(k));
	}
	
	/**
	 * Returns all registered statistics IDs.
	 * @return a Collection of HTTPCompressionInfo bean
	 */
	public static synchronized Collection<HTTPCompressionInfo> getInfo() {
		List<HTTPCompressionInfo> results = new ArrayList<HTTPCompressionInfo>(_stats.values());
		results.sort((s1, s2) -> s1._id.compareTo(s2._id));
		return results;
	}
	
	private HTTPCompressionInfo(String id) {
		super(); 
		_id = id;
	}
	
	/**
	 * Returns the statistics ID.
	 * @return the ID
	 */
	public String getID() {
		return _id;
	}

	/**
	 * Returns the total amount of compressed data transferred.
	 * @return the number of compressed bytes
	 */
	public long getRawSize() {
		return _rawBytes;
	}
	
	/**
	 * Returns the total amount of ucompressed data transferred.
	 * @return the number of ucompressed bytes
	 */
	public long getSize() {
		return _totalBytes;
	}
	
	/**
	 * Returns the number of times this DAO has been called.
	 * @return the number of requests
	 */
	public int getRequests() {
		return _reqs;
	}
	
	/**
	 * Updates the amount of data transferred.
	 * @param size the number of compressed bytes
	 */
	public synchronized void updateRaw(int size) {
		_rawBytes += size;
	}
	
	/**
	 * Updates the amount of data transferred.
	 * @param size the number of uncompressed bytes
	 */
	public synchronized void updateTotal(int size) {
		_totalBytes += size;
		_reqs++;
	}
	
	@Override
	public int hashCode() {
		return _id.hashCode();
	}
}