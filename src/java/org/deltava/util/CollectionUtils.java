// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.util;

import java.util.*;
import java.lang.reflect.Method;

/**
 * A utility class for dealing with Collections and Lists.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CollectionUtils {

	// Singleton constructor
	private CollectionUtils() {
	}

	/**
	 * A null-safe mechanism to determine if a collection has no data.
	 * @param c the Collection to check
	 * @return TRUE if c is null or has no elements, otherwise FALSE
	 */
	public static boolean isEmpty(Collection c) {
		return ((c == null) || (c.isEmpty()));
	}

	/**
	 * Determines which elements are contained within only one Collection.
	 * @param c1 the Collection to examine
	 * @param c2 the entries to check
	 * @return a List of entries contained within c1, but not c2
	 * @throws NullPointerException if c1 or c2 are null
	 */
	public static Collection getDelta(Collection c1, Collection c2) {

		// Convert the first collection to a List to preserve data
		List l1 = new ArrayList(c1);

		// Remove the entries from the second collection and return
		l1.removeAll(c2);
		return l1;
	}

	/**
	 * Compares two Collections and strips the values present in the other. <i>Note that this method does NOT
	 * preserve data; the Collections are altered via this method. </i>
	 * @param c1 the first Collection of entries
	 * @param c2 the first Collection of entries
	 * @throws NullPointerException if c1 or c2 are null
	 */
	public static void setDelta(Collection c1, Collection c2) {
		List tmpC1 = new ArrayList(c1); // we copy c1 since we modify it before c2.removeAll()
		c1.removeAll(c2);
		c2.removeAll(tmpC1);
	}

	/**
	 * Compares two Collections to see if there are any differences between them. This is done by comparing
	 * their sizes, and then calling c1.containsAll(c2) to ensure that all of c2 is contained within c1.
	 * @param c1 the first Collection of entries
	 * @param c2 the second Collection of entries
	 * @return TRUE if c1.size() != c2.size() or !c1.containsAll(c2), otherwise FALSE
	 */
	public static boolean hasDelta(Collection c1, Collection c2) {
		return ((c1.size() != c2.size()) || (!c1.containsAll(c2)));
	}

	/**
	 * A null-safe mechanism for converting a String array into a Collection.
	 * @param strValues the values to load into a List, usually from an HTTP request
	 * @param defltValues a Collection of values if strValues is null
	 * @return strValues converted to a List, or defltValues if strValues is null
	 * @see Arrays#asList(Object[])
	 */
	public static Collection loadList(String[] strValues, Collection defltValues) {
		return (strValues != null) ? Arrays.asList(strValues) : defltValues;
	}

	/**
	 * Converts a Collection into a Map.
	 * @param values the values to use
	 * @param keyProperty the property to call on each value to get the key value
	 * @return a Map of the values, indexed by their key
	 */
	public static Map createMap(Collection values, String keyProperty) {

		Map results = new HashMap();
		for (Iterator i = values.iterator(); i.hasNext(); ) {
			Object obj = i.next();
			try {
				Method m = obj.getClass().getMethod(StringUtils.getPropertyMethod(keyProperty), null);
				results.put(m.invoke(obj, null), obj);
			} catch (Exception e) { }
		}
		
		return results;
	}
}