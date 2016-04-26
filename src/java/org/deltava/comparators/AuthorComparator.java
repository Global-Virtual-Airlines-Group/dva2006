// Copyright 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import java.util.Comparator;

import org.deltava.beans.AuthoredBean;

/**
 * A Comparator to sort AuthoredBean objects.
 * @author Luke
 * @version 7.0
 * @since 3.1
 */

public class AuthorComparator implements Comparator<AuthoredBean>, java.io.Serializable {
	
	private final boolean _isReverse;

	/**
	 * Creates a new AuthorComparator.
	 */
	public AuthorComparator() {
		this(false);
	}

	/**
	 * Creates a new Comparator with an optional reverse sort.
	 * @param isReverse TRUE if a reverse sort, otherwise FALSE
	 */
	public AuthorComparator(boolean isReverse) {
		super();
		_isReverse = isReverse; 
	}
	
	/**
	 * Compares two AuthoredBeans by comparing their Author IDs.
	 */
	@Override
	public int compare(AuthoredBean ab1, AuthoredBean ab2) {
		int tmpResult = Integer.valueOf(ab1.getAuthorID()).compareTo(Integer.valueOf(ab2.getAuthorID()));
		return _isReverse ? (tmpResult * -1) : tmpResult;
	}
}