// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import java.util.*;

import org.deltava.beans.Staff;

import org.deltava.util.CollectionUtils;

/**
 * A Comparator for Staff Profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class StaffComparator<T extends Staff> extends AbstractComparator<T> {

	public static final int SORT = 0;
	public static final int AREA = 1;
	public static final int LASTNAME = 2;

	public static final String[] TYPES = { "Sort Order", "Functional Area", "Last Name" };

	private List<String> _areas;

	/**
	 * Initializes the comparator.
	 * @param comparisonType the comparison type code
	 * @throws IllegalArgumentException if an invalide code is specified
	 * @see StaffComparator#StaffComparator(String)
	 */
	public StaffComparator(int comparisonType) {
		super(TYPES);
		setComparisonType(comparisonType);
	}

	/**
	 * Initializes the comparator.
	 * @param comparisonType the comparison type label
	 * @throws IllegalArgumentException if an invalid label is specified
	 * @see StaffComparator#StaffComparator(int)
	 */
	public StaffComparator(String comparisonType) {
		super(TYPES);
		setComparisonType(comparisonType);
	}
	
	/**
	 * Returns wether functional areas have been loaded.
	 * @return TRUE if areas are present, otherwise FALSE
	 */
	public boolean hasAreas() {
		return !CollectionUtils.isEmpty(_areas);
	}

	/**
	 * Initializes the functional areas.
	 * @param areas an ordered Collection of area names
	 * @see StaffComparator#AREA
	 */
	public void setAreas(Collection<String> areas) {
		if (areas != null)
			_areas = new ArrayList<String>(areas);
	}

	/**
	 * Compares two Staff Profiles by the designated criteria.
	 * @throws ClassCastException if either object is not a Staff
	 * @see java.util.Comparator#compare(Object, Object)
	 */
	protected int compareImpl(T o1, T o2) {
		switch (_comparisonType) {
			case AREA:
				if (_areas != null) {
					int a1 = _areas.indexOf(o1.getArea());
					int a2 = _areas.indexOf(o2.getArea());
					int tmpResult = new Integer(a1).compareTo(new Integer(a2));
					if (tmpResult != 0)
						return tmpResult;
				}

			case SORT:
				int tmpResult = new Integer(o1.getSortOrder()).compareTo(new Integer(o2.getSortOrder()));
				if (tmpResult != 0)
					return tmpResult;

			default:
			case LASTNAME:
				return o1.getLastName().compareTo(o2.getLastName());
		}
	}
}