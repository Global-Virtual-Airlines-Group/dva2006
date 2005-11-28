package org.deltava.comparators;

import org.deltava.beans.Person;

/**
 * A comparator for sorting Pilot and Applicant objects.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PersonComparator<T extends Person> extends AbstractComparator<T> {

    public static final int FIRSTNAME = 0;
    public static final int LASTNAME = 1;
    public static final int LASTLOGIN = 2;
    public static final int CREATED = 3;

    public static final String[] TYPES = { "First Name", "Last Name", "Login Date", "Creation Date" };

    /**
     * @param comparisonType
     */
    public PersonComparator(int comparisonType) {
        super(TYPES);
        setComparisonType(comparisonType);
    }

    /**
     * @param comparisonType
     */
    public PersonComparator(String comparisonType) {
        super(TYPES);
        setComparisonType(comparisonType);
    }
    
    protected PersonComparator(String[] typeNames) {
        super(typeNames);
    }

    /**
     * Compares two person objects by the designated criteria.
     * @throws ClassCastException if either object is not a Person
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    protected int compareImpl(T p1, T p2) {

        int tmpResult;
        switch (_comparisonType) {
        case FIRSTNAME:
            tmpResult = p1.getFirstName().compareTo(p2.getFirstName());
            return (tmpResult == 0) ? p1.getLastName().compareTo(p2.getLastName()) : tmpResult;

        case LASTNAME:
            tmpResult = p1.getLastName().compareTo(p2.getLastName());
            return (tmpResult == 0) ? p1.getFirstName().compareTo(p2.getFirstName()) : tmpResult;

        case LASTLOGIN:
            if (p1.getLastLogin() == null) return -1;
            return p1.getLastLogin().compareTo(p2.getLastLogin());

        default:
        case CREATED:
            return p1.getCreatedOn().compareTo(p2.getCreatedOn());
        }
    }
}