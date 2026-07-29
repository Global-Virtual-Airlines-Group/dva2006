// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.ArrayList;

/**
 * A class to store lists with pagination metadata for use with paginated APIs.
 * @author Luke
 * @version 12.5
 * @since 12.5
 * @param <E> the type contained within this collection 
 */

public class PaginatedList<E> extends ArrayList<E> {
	
	private final int _offset;
	private int _count;
	private int _total;
	
	/**
	 * Creates the collection.
	 * @param offset the starting offset for this result set
	 */
	public PaginatedList(int offset) {
		super();
		_offset = offset;
	}
	
	/**
	 * Returns the starting offset for this result set.
	 * @return the starting offset
	 */
	public int getOffset() {
		return _offset;
	}
	
	/**
	 * Returns the number of results retrieved in this page. This may be greater than the size of this Collection, as entries may have been filtered out prior to being added. 
	 * @return the number of results
	 */
	public int getCount() {
		return _count;
	}
	
	/**
	 * Returns the total number of results in this reuslt set. This may be greater than the number of entries in this collection.
	 * @return the number of results
	 */
	public int getTotal() {
		return _total;
	}
	
	/**
	 * Updates the number of results retrieved in this page. This may be greater than the size of this Collection, as entries may have been filtered out prior to being added.
	 * @param cnt the number of results
	 */
	public void setCount(int cnt) {
		_count = cnt;
	}

	/**
	 * Updates the total number of results. This may be greater than the number of entries in this collection.
	 * @param total the total number of results
	 */
	public void setTotal(int total) {
		_total = total;
	}
}