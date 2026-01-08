// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.Comparator;

/**
 * A counter class that has a label and a counter.
 * @author Luke
 * @version 12.4
 * @since 12.4
 * @param <K> the label class 
 */

public class Count<K extends Comparable<K>> implements Comparable<Count<?>> {

	private final K _label;
	private int _cnt;
	
	/**
	 * Label comparator class.
	 * @param <K> the label class
	 */
	static class LabelComparator<K extends Comparable<K>> implements Comparator<Count<K>> {

		@Override
		public int compare(Count<K> c1, Count<K> c2) {
			return c1._label.compareTo(c2._label);
		}
	}
	
	/**
	 * Returns a comparator that compares based on label, rather than value.
	 * @param c the generic class
	 * @return a Comparator
	 */
	public static <K extends Comparable<K>> Comparator<Count<K>> labelComparator(Class<K> c) {
		return new LabelComparator<K>();
	}
	
	/**
	 * Creates the counter.
	 * @param label the label
	 */
	public Count(K label) {
		super();
		_label = label;
	}
	
	/**
	 * Increments the counter.
	 */
	public void inc() {
		_cnt++;
	}

	/**
	 * Returns the counter label.
	 * @return the label
	 */
	public K getLabel() {
		return _label;
	}
	
	/**
	 * Returns the counter value.
	 * @return the value
	 */
	public int getValue() {
		return _cnt;
	}

	@Override
	public int compareTo(Count<?> c2) {
		return Integer.compare(_cnt, c2._cnt);
	}
	
	@Override
	public int hashCode() {
		return _label.hashCode();
	}
}