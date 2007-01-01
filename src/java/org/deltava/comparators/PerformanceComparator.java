// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import org.deltava.beans.stats.PerformanceMetrics;

/**
 * A Comparator to sort Performance Metrics beans.
 * @author Luke
 * @version 1.0
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
	protected int compareImpl(PerformanceMetrics m1, PerformanceMetrics m2) {
		switch (_comparisonType) {
			case NAME:
				return m1.getName().compareTo(m2.getName());

			case AVERAGE:
				return new Double(m1.getAverage()).compareTo(new Double(m2.getAverage()));

			case MAX:
				return new Double(m1.getMaximum()).compareTo(new Double(m2.getMaximum()));

			case MIN:
				return new Double(m1.getMinimum()).compareTo(new Double(m2.getMinimum()));

			case COUNT:
				return new Long(m1.getCount()).compareTo(new Long(m2.getCount()));

			default:
				return m1.compareTo(m2);
		}
	}
}