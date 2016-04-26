// Copyright 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.time.Instant;

import org.deltava.beans.DatabaseBean;

/**
 * A bean to store ACARS position archive metadata.
 * @author Luke
 * @version 7.0
 * @since 6.2
 */

public class ArchiveMetadata extends DatabaseBean {
	
	private int _positionCount;
	private Instant _archivedOn;
	private long _crc;
	private int _size;

	/**
	 * Creates the bean.
	 * @param id the ACARS flight ID
	 */
	public ArchiveMetadata(int id) {
		super();
		setID(id);
	}

	/**
	 * Returns the CRC32 value for the archived data.
	 * @return the CRC32 or zero if none
	 */
	public long getCRC32() {
		return _crc;
	}
	
	/**
	 * Returns the size of the archived data.
	 * @return the size in bytes
	 */
	public int getSize() {
		return _size;
	}
	
	/**
	 * Returns the date the flight was archived.
	 * @return the archive date/time
	 */
	public Instant getArchivedOn() {
		return _archivedOn;
	}
	
	/**
	 * Returns the number of archived position entries.
	 * @return the number of entries
	 */
	public int getPositionCount() {
		return _positionCount;
	}

	/**
	 * Updates the CRC32 value for the archived data.
	 * @param crc the CRC32
	 */
	public void setCRC32(long crc) {
		_crc = crc;
	}
	
	/**
	 * Updates the size of the archived metadata.
	 * @param size the size in bytes
	 */
	public void setSize(int size) {
		_size = Math.max(0, size);
	}
	
	/**
	 * Updates the archive date.
	 * @param dt the archive date/time
	 */
	public void setArchivedOn(Instant dt) {
		_archivedOn = dt;
	}

	/**
	 * Updates the number of archive position entries.
	 * @param cnt the number of position entries
	 */
	public void setPositionCount(int cnt) {
		_positionCount = Math.max(0, cnt);
	}
}