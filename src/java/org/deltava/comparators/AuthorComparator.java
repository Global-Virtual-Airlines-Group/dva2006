// Copyright 2010, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import java.util.Comparator;

import org.deltava.beans.AuthoredBean;

/**
 * A Comparator to sort AuthoredBean objects.
 * @author Luke
 * @version 8.0
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
	
	@Override
	public int compare(AuthoredBean ab1, AuthoredBean ab2) {
		int tmpResult = Integer.compare(ab1.getAuthorID(), ab2.getAuthorID());
		return _isReverse ? (tmpResult * -1) : tmpResult;
	}
}