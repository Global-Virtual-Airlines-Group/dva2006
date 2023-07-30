// Copyright 2013, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.navdata;

import java.time.Instant;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store Gate/Terminal Route archive metadata.
 * @author Luke
 * @version 11.1
 * @since 10.0
 */

class Metadata implements Cacheable {
	
	private final String _fileName;
	private final String _hash;
	private final String _hashType;
	private int _airportCount;
	private final Instant _created = Instant.now();
	
	/**
	 * Creates the metadata for a file.
	 * @param fileName the file name
	 * @param hash the archive hash
	 * @param hashType the hash algorithm
	 */
	Metadata(String fileName, String hash, String hashType) {
		super();
		_fileName = fileName;
		_hash = hash;
		_hashType = hashType;
	}
	
	/**
	 * Returns the file name.
	 * @return the file name
	 */
	public String getFileName() {
		return _fileName;
	}
	
	/**
	 * Returns the archive hash.
	 * @return the hash value
	 */
	public String getHash() {
		return _hash;
	}

	/**
	 * Returns the archive hash algorithm.
	 * @return the algorithm ame
	 */
	public String getHashType() {
		return _hashType;
	}
	
	/**
	 * Returns the number of Airports in the archive.
	 * @return the number of Airports
	 */
	public int getAirportCount() {
		return _airportCount;
	}
	
	/**
	 * Returns the archive creation date.
	 * @return the creation date/time
	 */
	public Instant getCreatedOn() {
		return _created;
	}

	/**
	 * Updates the number of Airports in the archive. 
	 * @param cnt the number of Airports
	 */
	public void setAirportCount(int cnt) {
		_airportCount = cnt;
	}

	@Override
	public Object cacheKey() {
		return _fileName;
	}
}