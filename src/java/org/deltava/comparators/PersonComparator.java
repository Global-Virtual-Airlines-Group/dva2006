// Copyright 2005, 2006, 2009, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import java.time.Instant;

import org.deltava.beans.Person;

/**
 * A comparator for sorting Pilot and Applicant objects.
 * @author Luke
 * @version 8.0
 * @since 1.0
 * @param <T> the Person subclass
 */

public class PersonComparator<T extends Person> extends AbstractComparator<T> {

	public static final int FIRSTNAME = 0;
	public static final int LASTNAME = 1;
	public static final int LASTLOGIN = 2;
	public static final int CREATED = 3;

	public static final String[] TYPES = { "First Name", "Last Name", "Login Date", "Creation Date" };

	/**
	 * Initializes the comparator.
	 * @param comparisonType the comparison type code
	 * @throws IllegalArgumentException if an invalide code is specified
	 * @see PersonComparator#PersonComparator(String)
	 */
	public PersonComparator(int comparisonType) {
		super(TYPES);
		setComparisonType(comparisonType);
	}

	/**
	 * Initializes the comparator.
	 * @param comparisonType the comparison type label
	 * @throws IllegalArgumentException if an invalid label is specified
	 * @see PersonComparator#PersonComparator(int)
	 */
	public PersonComparator(String comparisonType) {
		super(TYPES);
		setComparisonType(comparisonType);
	}

	/**
	 * Initializes the comparator.
	 * @param typeNames an array of type names
	 */
	protected PersonComparator(String[] typeNames) {
		super(typeNames);
	}

	@Override
	protected int compareImpl(T p1, T p2) {

		switch (_comparisonType) {
		case FIRSTNAME:
			int tmpResult = p1.getFirstName().compareTo(p2.getFirstName());
			return (tmpResult == 0) ? p1.getLastName().compareTo(p2.getLastName()) : tmpResult;

		case LASTNAME:
			tmpResult = p1.getLastName().compareTo(p2.getLastName());
			return (tmpResult == 0) ? p1.getFirstName().compareTo(p2.getFirstName()) : tmpResult;

		case LASTLOGIN:
			Instant ll1 = p1.getLastLogin();
			Instant ll2 = p2.getLastLogin();
			if (ll1 == null)
				ll1 = p1.getCreatedOn();
			if (ll2 == null)
				ll2 = p2.getCreatedOn();

			tmpResult = ll1.compareTo(ll2);
			return (tmpResult == 0) ? Integer.compare(p1.getID(), p2.getID()) : tmpResult;

		default:
		case CREATED:
			return p1.getCreatedOn().compareTo(p2.getCreatedOn());
		}
	}
}