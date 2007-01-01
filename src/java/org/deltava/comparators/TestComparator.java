// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import org.deltava.beans.testing.*;

/**
 * A comparator for sorting Examination and Check Ride objects.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class TestComparator extends AbstractComparator<Test> {

	public static final int DATE = 0;
	public static final int SCORE = 1;
	public static final int PERCENT = 2;
	public static final int TYPE = 3;

	public static final String[] TYPES = { "Date", "Score", "Percentage", "Type" };

    /**
     * Creates a new TestComparator with a given comparison type.
     * @param comparisonType The criteria by which to compare
     * @throws IllegalArgumentException if the type is invalid
     * @see AbstractComparator#setComparisonType(int)
     */
	public TestComparator(int comparisonType) {
		super(TYPES);
		setComparisonType(comparisonType);
	}

    /**
     * Creates a new TestComparator with a given comparison type.
     * @param comparisonType The criteria type name by which to compare
     * @throws IllegalArgumentException if the type name is invalid
     * @see AbstractComparator#setComparisonType(String)
     */
	public TestComparator(String comparisonType) {
		super(TYPES);
		setComparisonType(comparisonType);
	}

	/**
     * Compares two examinations/checkrides by the designated criteria.
     * @throws ClassCastException if either object is not a Test 
     * @see java.util.Comparator#compare(Object, Object)
     */
	protected int compareImpl(Test t1, Test t2) {
		switch (_comparisonType) {
			case DATE:
				return t1.getDate().compareTo(t2.getDate());
				
			case SCORE:
				return new Integer(t1.getScore()).compareTo(new Integer(t2.getScore()));

			case PERCENT:
				double pct1 = (t1 instanceof Examination) ? (t1.getScore() / t1.getSize()) : t1.getScore();
				double pct2 = (t2 instanceof Examination) ? (t2.getScore() / t2.getSize()) : t2.getScore();
				return new Double(pct1).compareTo(new Double(pct2));

			case TYPE:
				return t1.getClass().getName().compareTo(t2.getClass().getName());
				
			default:
				return 0;
		}
	}
}