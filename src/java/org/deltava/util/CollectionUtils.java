// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.*;
import java.lang.reflect.Method;

/**
 * A utility class for dealing with Collections and Lists.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class CollectionUtils {

	// Singleton constructor
	private CollectionUtils() {
		super();
	}

	/**
	 * A null-safe mechanism to determine if a collection has no data.
	 * @param c the Collection to check
	 * @return TRUE if c is null or has no elements, otherwise FALSE
	 */
	public static boolean isEmpty(Collection<?> c) {
		return ((c == null) || (c.size() == 0));
	}
	
	/**
	 * Returns the last object in a Collection, using its natural iteration order.
	 * @param c the Collection
	 * @return the last entry, or null if the collection is empty
	 */
	public static <T> T getLast(Collection<T> c) {
		if (isEmpty(c))
			return null;
		
		// Reverse the set
		List<T> l = new ArrayList<T>(c);
		Collections.reverse(l);
		return l.get(0);
	}

	/**
	 * Determines which elements are contained within only one Collection.
	 * @param c1 the Collection to examine
	 * @param c2 the entries to check
	 * @return a List of entries contained within c1, but not c2
	 * @throws NullPointerException if c1 or c2 are null
	 */
	public static <T> Collection<T> getDelta(Collection<T> c1, Collection<T> c2) {

		// Convert the first collection to a List to preserve data
		Collection<T> l1 = new ArrayList<T>(c1);

		// Remove the entries from the second collection and return
		l1.removeAll(c2);
		return l1;
	}
	
	/**
	 * Merges a number of Collections, stripping out duplicate etnries.
	 * @param entries an array of Collections
	 * @return a Collection of the unique entries across all Collections
	 */
	public static <T> Collection<T> merge(Collection<T>... entries) {
		Collection<T> results = new LinkedHashSet<T>();
		for (int x = 0; x < entries.length; x++) {
			Collection<T> c = entries[x];
			results.addAll(c);
		}
		
		return results;
	}

	/**
	 * Compares two Collections and strips the values present in the other. <i>Note that this method does NOT
	 * preserve data; the Collections are altered via this method. </i>
	 * @param c1 the first Collection of entries
	 * @param c2 the first Collection of entries
	 * @throws NullPointerException if c1 or c2 are null
	 */
	public static <T> void setDelta(Collection<T> c1, Collection<T> c2) {
		List<T> tmpC1 = new ArrayList<T>(c1); // we copy c1 since we modify it before c2.removeAll()
		c1.removeAll(c2);
		c2.removeAll(tmpC1);
	}
	
	/**
	 * Compares two collections and returns the number of elements present in both Collections.
	 * @param c1 the first Collection
	 * @param c2 the second Collection
	 * @return the number of entries present in both
	 * @throws NullPointerException if c1 or c2 are null
	 */
	public static <T> int hasMatches(Collection<T> c1, Collection<T> c2) {
		Collection<T> tmpC1 = new ArrayList<T>(c1);
		tmpC1.removeAll(c2);
		return (c1.size() - tmpC1.size());
	}

	/**
	 * Compares two Collections to see if there are any differences between them. This is done by comparing
	 * their sizes, and then calling c1.containsAll(c2) to ensure that all of c2 is contained within c1.
	 * @param c1 the first Collection of entries
	 * @param c2 the second Collection of entries
	 * @return TRUE if c1.size() != c2.size() or !c1.containsAll(c2), otherwise FALSE
	 */
	public static <T> boolean hasDelta(Collection<T> c1, Collection<T> c2) {
		return ((c1.size() != c2.size()) || (!c1.containsAll(c2)));
	}

	/**
	 * A null-safe mechanism for converting a String array into a Collection.
	 * @param strValues the values to load into a List, usually from an HTTP request
	 * @param defltValues a Collection of values if strValues is null
	 * @return strValues converted to a List, or defltValues if strValues is null
	 * @see Arrays#asList(Object[])
	 */
	public static Collection<String> loadList(String[] strValues, Collection<String> defltValues) {
		return new LinkedHashSet<String>((strValues != null) ? Arrays.asList(strValues) : defltValues);
	}

	/**
	 * Converts a Collection into a Map.
	 * @param values the values to use
	 * @param keyProperty the property to call on each value to get the key value
	 * @return a Map of the values, indexed by their key
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> createMap(Collection<V> values, String keyProperty) {
		Method m = null;
		Map<K, V> results = new LinkedHashMap<K, V>();
		for (Iterator<V> i = values.iterator(); i.hasNext(); ) {
			V obj = i.next();
			try {
				if (m == null)
					m = obj.getClass().getMethod(StringUtils.getPropertyMethod(keyProperty), (Class []) null);
				
				Object key = m.invoke(obj, (Object []) null);
				results.put((K) key, obj);
			} catch (Exception e) {
				// empty
			}
		}
		
		return results;
	}
	
	/**
	 * Converts two Collections into a Map. The resulting Map will be the same size as the smallest of
	 * the two supplied Collections, and the key/value pairs will be assigned in the normal iteration order
	 * of each Collection. 
	 * @param keys a Collection of key objects
	 * @param values a Collection of value objects
	 * @return a Map of key/value pairs.
	 */
	public static <K, V> Map<K, V> createMap(Collection<K> keys, Collection<V> values) {
		Map<K, V> results = new LinkedHashMap<K, V>();
		Iterator<V> vi = values.iterator();
		for (Iterator<K> ki = keys.iterator(); ki.hasNext() && vi.hasNext(); ) {
			K key = ki.next();
			V value = vi.next();
			results.put(key, value);
		}
		
		return results;
	}
	
	/**
	 * Sorts a Collection using a particular Comparator.
	 * @param c the Collection to sort
	 * @param cmp the Comparator
	 * @return a sorted Collection
	 */
	public static <T> List<T> sort(Collection<T> c, Comparator<T> cmp) {
		List<T> values = new ArrayList<T>(c);
		Collections.sort(values, cmp);
		return values;
	}
	
	/**
	 * Examines two Collections and returns the items present in both.
	 * @param c1 the first Collection
	 * @param c2 the second Collection
	 * @return a Collection of items present in both c1 and c2
	 */
	public static <T> Collection<T> union(Collection<T> c1, Collection<T> c2) {
		List<T> values = new ArrayList<T>(c1);
		values.removeAll(c2);
		List<T> results = new ArrayList<T>(c1);
		results.removeAll(values);
		return results;
	}
}