// Copyright 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.time.Instant;

import org.apache.commons.pool2.impl.DefaultPooledObjectInfo;

/**
 * A bean to wrap Apache pooled object data. 
 * @author Luke
 * @version 11.3
 * @since 11.3
 */

public class PoolConnectionInfo implements java.io.Serializable, Comparable<PoolConnectionInfo> {
	
	private final int _id;
	private final long _borrowCount;
	private final String _type;
	private final Instant _created;
	private final Instant _lastBorrowed;
	private final Instant _lastReturned;

	/**
	 * Creates the bean.
	 * @param id the connection ID
	 * @param info the object info
	 */
	public PoolConnectionInfo(int id, DefaultPooledObjectInfo info) {
		super();
		_id = id;
		_borrowCount = info.getBorrowedCount();
		_type = info.getPooledObjectType();
		_created = Instant.ofEpochMilli(info.getCreateTime());
		_lastBorrowed = Instant.ofEpochMilli(info.getLastBorrowTime());
		_lastReturned = Instant.ofEpochMilli(info.getLastReturnTime());
	}
	
	/**
	 * Returns the connection ID.
	 * @return the ID
	 */
	public int getID() {
		return _id;
	}
	
	/**
	 * Returns whether the connection is currently active.
	 * @return TRUE if active, otherwise FALSE
	 */
	public boolean isActive() {
		return (_lastBorrowed != null) && (_lastReturned != null) && _lastReturned.isBefore(_lastBorrowed);
	}
	
	/**
	 * Returns the number of times this connection has been used.
	 * @return the use count
	 */
	public long getBorrowCount() {
		return _borrowCount;
	}
	
	/**
	 * Returns the pooled object type name.
	 * @return the type name
	 */
	public String getType() {
		return _type;
	}
	
	/**
	 * Returns the date/time this connection was created.
	 * @return the creation date/time
	 */
	public Instant getCreated() {
		return _created;
	}

	/**
	 * Returns the date/time this connection was last retrieved from the pool.
	 * @return the retrieval date/time
	 */
	public Instant getLastBorrowed() {
		return _lastBorrowed;
	}
	
	/**
	 * Returns the date/time this connection was last returned to the pool.
	 * @return the return date/time
	 */
	public Instant getLastReturned() {
		return _lastReturned;
	}
	
	@Override
	public String toString() {
		return String.format("%s-%d", _type, Integer.valueOf(_id));
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public int compareTo(PoolConnectionInfo i2) {
		return Integer.compare(_id, i2._id);
	}
}