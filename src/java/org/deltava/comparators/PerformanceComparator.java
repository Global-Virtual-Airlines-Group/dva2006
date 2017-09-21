// Copyright 2006, 2010, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import org.deltava.beans.stats.PerformanceMetrics;

/**
 * A Comparator to sort Performance Metrics beans.
 * @author Luke
 * @version 8.0
 * @since 1.0
 */

public class PerformanceComparator extends AbstractComparator<PerformanceMetrics> {

	public static final int NAME = 0;
	public static final int AVERAGE = 1;
	public static final int MAX = 2;
	public static final int MIN = 3;
	public static final int COUNT = 4;

	private static final String[] TYPES = { "Category", "Average", "Maximum", "Minimum", "Count" };

	/**
	 * Creates a new comparator with a particular comparison type code.
	 * @param comparisonType the comparison type code
	 * @throws IllegalArgumentException if the type is invalid
	 * @see AbstractComparator#setComparisonType(int)
	 */
	public PerformanceComparator(int comparisonType) {
		super(TYPES);
		setComparisonType(comparisonType);
	}

	/**
	 * Creates a new comparator with a particular comparison type name.
	 * @param comparisonType the comparison type name
	 * @throws IllegalArgumentException if the type is invalid
	 * @see AbstractComparator#setComparisonType(String)
	 */
	public PerformanceComparator(String comparisonType) {
		super(TYPES);
		setComparisonType(comparisonType);
	}

	/**
	 * Compares two PerformanceMetrics beans by the designated criteria.
	 * @throws ClassCastException if either object is not a PerformanceMetrics bean
	 * @see java.util.Comparator#compare(Object, Object)
	 */
	@Override
	protected int compareImpl(PerformanceMetrics m1, PerformanceMetrics m2) {
		switch (_comparisonType) {
			case NAME:
				return m1.getName().compareTo(m2.getName());

			case AVERAGE:
				return Double.compare(m1.getAverage(), m2.getAverage());

			case MAX:
				return Double.compare(m1.getMaximum(), m2.getMaximum());

			case MIN:
				return Double.compare(m1.getMinimum(), m2.getMinimum());

			case COUNT:
				return Long.compare(m1.getCount(), m2.getCount());

			default:
				return m1.compareTo(m2);
		}
	}
}