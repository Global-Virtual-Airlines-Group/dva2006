// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import java.util.*;

import org.deltava.beans.DatabaseBean;

/**
 * A Comparator to order database beans in an arbitrary order, based on a pre-set
 * list of IDs. Database beans with IDs not in the list will be added at the end of
 * the Collection. 
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public class ArbitraryComparator implements Comparator<DatabaseBean> {

	private final List<Integer> _IDs = new ArrayList<Integer>();
	
	/**
	 * Initializes the comparator.
	 * @param IDs a Collection of database IDs in the proper order.
	 */
	public ArbitraryComparator(Collection<Integer> IDs) {
		super();
		_IDs.addAll(IDs);
	}

	/**
	 * Compares two database beans.
	 */
	public int compare(DatabaseBean db1, DatabaseBean db2) {
		
		// Get the two offsets
		int ofs1 = _IDs.indexOf(Integer.valueOf(db1.getID()));
		int ofs2 = _IDs.indexOf(Integer.valueOf(db2.getID()));
		
		// Compare them
		int tmpResult = Integer.valueOf(ofs1).compareTo(Integer.valueOf(ofs2));
		return (tmpResult == 0) ? Integer.valueOf(db1.getID()).compareTo(Integer.valueOf(db2.getID())) : tmpResult;
	}
}