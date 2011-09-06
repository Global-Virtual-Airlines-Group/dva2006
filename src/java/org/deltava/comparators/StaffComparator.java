// Copyright 2006, 2008, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import java.util.*;

import org.deltava.beans.Staff;

/**
 * A Comparator for Staff Profiles.
 * @author Luke
 * @version 4.0
 * @since 1.0
 */

public class StaffComparator extends AbstractComparator<Staff> {

	public static final int SORT = 0;
	public static final int AREA = 1;
	public static final int LASTNAME = 2;

	public static final String[] TYPES = { "Sort Order", "Functional Area", "Last Name" };

	private final List<String> _areas = new ArrayList<String>();

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
	 * Returns whether functional areas have been loaded.
	 * @return TRUE if areas are present, otherwise FALSE
	 */
	public boolean hasAreas() {
		return !_areas.isEmpty();
	}

	/**
	 * Initializes the functional areas.
	 * @param areas an ordered Collection of area names
	 * @see StaffComparator#AREA
	 */
	public void setAreas(Collection<String> areas) {
		if (areas != null)
			_areas.addAll(areas);
	}

	/**
	 * Compares two Staff Profiles by the designated criteria.
	 * @throws ClassCastException if either object is not a Staff
	 * @see java.util.Comparator#compare(Object, Object)
	 */
	protected int compareImpl(Staff o1, Staff o2) {
		int tmpResult = 0;
		switch (_comparisonType) {
			case AREA:
				int a1 = _areas.indexOf(o1.getArea());
				int a2 = _areas.indexOf(o2.getArea());
				tmpResult = Integer.valueOf(a1).compareTo(Integer.valueOf(a2));
				if (tmpResult == 0)
					tmpResult = Integer.valueOf(o1.getSortOrder()).compareTo(Integer.valueOf(o2.getSortOrder()));
				if (tmpResult == 0)
					tmpResult = o1.getRank().compareTo(o2.getRank()) * -1;
				if (tmpResult == 0)
					tmpResult = o1.getEquipmentType().compareTo(o2.getEquipmentType());
					
				return tmpResult;

			case SORT:
				tmpResult = Integer.valueOf(o1.getSortOrder()).compareTo(Integer.valueOf(o2.getSortOrder()));
				if (tmpResult == 0)
					tmpResult = o1.getLastName().compareTo(o2.getLastName());
				
				return tmpResult;

			default:
			case LASTNAME:
				return o1.getLastName().compareTo(o2.getLastName());
		}
	}
}