// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

import java.time.Instant;

/**
 * A bean to store weather tile layer dates and paths. 
 * @author Luke
 * @version 11.6
 * @since 11.6
 */

public class TileDate implements Comparable<TileDate>, java.io.Serializable {
	
	private final Instant _dt;
	private final String _path;

	/**
	 * Creates the bean.
	 * @param dt the effective date/time
	 * @param path the tile path on the remote server
	 */
	public TileDate(Instant dt, String path) {
		super();
		_dt = dt;
		_path = path;
	}
	
	/**
	 * Returns the effective date.
	 * @return the date/time
	 */
	public Instant getDate() {
		return _dt;
	}
	
	/**
	 * Returns the layer path.
	 * @return the path
	 */
	public String getPath() {
		return _path;
	}

	@Override
	public int compareTo(TileDate td) {
		return _dt.compareTo(td._dt);
	}
}