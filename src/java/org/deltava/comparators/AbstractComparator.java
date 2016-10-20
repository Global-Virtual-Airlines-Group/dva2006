// Copyright 2005, 2006, 2008, 2010, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import java.util.Comparator;

import org.deltava.util.StringUtils;

/**
 * Implements common comparator functions.
 * NOTE: Most comparators in this package <b>impose orderings that are inconsistent with equals</b>.
 * @author Luke
 * @version 7.2
 * @since 1.0
 * @param <T> the object class
 */

public abstract class AbstractComparator<T> implements Comparator<T>, java.io.Serializable {

    /**
     * The criteria by which to compare objects.
     */
    protected int _comparisonType;
    
    /**
     * Text descriptions of the different comparison types.
     */
    protected String[] _typeNames;
    
    /**
     * Create a new comparator with a given set of type names.
     * @param typeNames An array of type names
     * @throws NullPointerException if typeNames is null
     */
    protected AbstractComparator(String[] typeNames) {
        super();
        _typeNames = typeNames;
        if (_typeNames == null)
            throw new NullPointerException();
    }

    /**
     * This method does the actual comparison. It is called and then we modify the result if we are performing
     * a reverse sort.
     * @param o1 The first object to compare
     * @param o2 The second object to compare
     * @return -1, 0, 1 as defined by the compareTo(Object, Object) interface
     * @throws ClassCastException if the object types are not supported by this implementation
     * @see AbstractComparator#compare(Object, Object)
     * @see Comparator#compare(java.lang.Object, java.lang.Object)
     */
    protected abstract int compareImpl(T o1, T o2);
    
    /**
     * Sets the comparison type.
     * @param type The comparison type.
     * @throws IllegalArgumentException if the type is negative or > typeNames.length
     * @see AbstractComparator#getComparisonType()
     */
    public void setComparisonType(int type) {
        if ((type < 0) || (type > _typeNames.length))
            throw new IllegalArgumentException("Comparison type must be >= 0 && <= " + _typeNames.length);
        
        _comparisonType = type;
    }
    
    /**
     * Sets the comparison type.
     * @param type The comparison type, contained within _typeNames
     * @throws IllegalArgumentException if the type is not found in _typeNames
     * @see AbstractComparator#getTypeNames()
     */
    public void setComparisonType(String type) {
    	int typeCode = StringUtils.arrayIndexOf(_typeNames, type);
    	if (typeCode == -1)
    		throw new IllegalArgumentException("Invalid comparison type - " + type);
    	
    	_comparisonType = typeCode;
    }

    /**
     * Return the list of comparison type names.
     * @return an array of type names
     */
    public String[] getTypeNames() {
        return _typeNames;
    }
    
    /**
     * Return the comparison type.
     * @return The comparison type in use
     * @see AbstractComparator#setComparisonType(int)
     * @see AbstractComparator#setComparisonType(String)
     */
    public int getComparisonType() {
        return _comparisonType;
    }
    
    /**
     * Return the result by interrogating the implementation and applying a reverse sort if requred.
     * @param o1 The first object to compare
     * @param o2 The second object to compare
     * @return -1, 0, 1 as defined by the compareTo(Object, Object) interface
     * @throws ClassCastException if the object types are not supported by this implementation
     * @see AbstractComparator#compareImpl(Object, Object)
     * @see Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public final int compare(T o1, T o2) {
        return compareImpl(o1, o2);
    }
}