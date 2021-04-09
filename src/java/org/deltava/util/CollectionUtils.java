// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2013, 2017, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.*;
import java.util.function.*;

/**
 * A utility class for dealing with Collections.
 * @author Luke
 * @version 10.0
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
	 * Converts a Collection into a Map.
	 * @param values the values to use
	 * @param f the function to call on each value to get the key value
	 * @return a Map of the values, indexed by their key
	 */
	public static <K, V> Map<K, V> createMap(Collection<V> values, Function<V, K> f) {
		Map<K, V> results = new LinkedHashMap<K, V>();
		values.forEach(v -> results.put(f.apply(v), v));
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
		for (Iterator<K> ki = keys.iterator(); ki.hasNext() && vi.hasNext(); )
			results.put(ki.next(), vi.next());
		
		return results;
	}
	
	/**
	 * Clones and sorts a Collection using a particular Comparator.
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
	
	/**
	 * Adds an entry to a Collection contained as a Map value.
	 * @param m the Map
	 * @param key the key
	 * @param entry the value
	 */
	public static <K, V> void addMapCollection(Map<K, Collection<V>> m, K key, V entry) {
		addMapCollection(m, key, entry, LinkedHashSet::new);
	}

	/**
	 * Adds an entry to a Collection contained as a Map value, allowing a specific Collection constructor.
	 * @param m the Map
	 * @param key the key
	 * @param entry the value
	 * @param cf the Collection {@link Supplier}
	 */
	public static <K, V> void addMapCollection(Map<K, Collection<V>> m, K key, V entry, Supplier<Collection<V>> cf) {
		Collection<V> c = m.get(key);
		if (c == null) {
			c = cf.get();
			m.put(key, c);
		}
		
		c.add(entry);		
	}
	/**
	 * Creates a Collection of non-null values.
	 * @param values the values to add
	 * @return a Collection of non-null values
	 */
	@SafeVarargs
	public static <T> Collection<T> nonNull(T... values) {
		return nonNull(ArrayList::new, values);
	}
	
	/**
	 * Creates a Collection of non-null values.
	 * @param cf the Collection factory function
	 * @param values the values to add
	 * @return a Collection of non-null values
	 */
	@SafeVarargs
	public static <T> Collection<T> nonNull(Supplier<Collection<T>> cf, T... values) {
		Collection<T> c = cf.get();
		for (T v : values) {
			if (v != null)
				c.add(v);
		}
		
		return c;
	}
}