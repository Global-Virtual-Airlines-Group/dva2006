// Copyright 2005, 2007, 2009, 2010, 2016, 2018, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.Helper;

/**
 * A utility class to generate combobox lists.
 * @author Luke
 * @version 9.0
 * @since 1.0
 * @see ComboAlias
 */

@Helper(ComboAlias.class)
public class ComboUtils {

    private ComboUtils() { // private constructor since we are all static
    	super();
    }

    private static class ComboAliasImpl implements java.io.Serializable, ComboAlias, Comparable<ComboAlias> {
        
        private final String _name;
        private final String _alias;
        
        ComboAliasImpl(String name, String alias) {
            super();
            _name = name;
            _alias = alias;
        }
        
        ComboAliasImpl(String name) {
            this(name, name);
        }
        
        @Override
		public String getComboAlias() {
            return _alias;
        }
        
        @Override
		public String getComboName() {
            return _name;
        }
        
        @Override
		public int compareTo(ComboAlias c2) {
        	int tmpResult = _name.compareTo(c2.getComboName());
        	return (tmpResult == 0) ? _alias.compareTo(c2.getComboAlias()) : tmpResult;
        }
        
        @Override
		public boolean equals(Object o2) {
        	return (o2 instanceof ComboAlias) ? (compareTo((ComboAlias) o2) == 0) : false;
        }
        
        @Override
		public int hashCode() {
        	return _alias.hashCode();
        }
    }

    /**
     * Create a list of ComboAlias objects from an array of Strings. The name/alias will be the same.
     * @param names a variable number of Strings
     * @return a List of ComboAlias objects
     * @see ComboUtils#fromList(Collection)
     */
    public static List<ComboAlias> fromArray(String... names) {
        return fromList(Arrays.asList(names));
    }
    
    /**
     * Create a list of ComboAlias objects from an array of Enumerations, propercasing the name.
     * @param names a variable number of enums
     * @return a List of ComboAlias objects
     */
    public static List<ComboAlias> properCase(Enum<?>... names) {
        List<ComboAlias> results = new ArrayList<ComboAlias>(names.length + 2);
        for (int x = 0; x < names.length; x++) {
        	String n = names[x].name();
        	String desc = n.substring(0, 1).toUpperCase() + n.substring(1).toLowerCase();
        	results.add(new ComboAliasImpl(desc, n));
        }
    	
    	return results;
    }

    /**
     * Create a ComboAlias from a String. The name/alias will be the same.
     * @param name the String
     * @return a ComboAlias object
     */
    public static ComboAlias fromString(String name) {
        return new ComboAliasImpl(name);
    }
    
    /**
     * Create a ComboAlias from a name & alias pair.
     * @param name the name
     * @param alias the alias
     * @return a ComboAlias object
     */
    public static ComboAlias fromString(String name, String alias) {
        return new ComboAliasImpl(name, alias);
    }
    
    /**
     * Create a list of ComboAlias objects from a List of objects. The name/alias will be the same.
     * @param names a List of names
     * @return a List of ComboAlias objects
     */
    public static List<ComboAlias> fromList(Collection<?> names) {
        return names.stream().map(n -> new ComboAliasImpl(String.valueOf(n))).collect(Collectors.toList());
    }
    
    /**
     * Create a list of ComboAlias objects from a Map. The keys will be the name, the values the aliases.
     * The aliases need not be Strings; the toString() method will be called on them to get a String representation.
     * @param names a Map of name/value pairs
     * @return a List of ComboAlias Objects
     * @see ComboUtils#fromArray(String[], Object[])
     * @see Object#toString()
     */
    public static List<ComboAlias> fromMap(Map<String, Object> names) {
        return names.entrySet().stream().map(me -> new ComboAliasImpl(me.getKey(), String.valueOf(me.getValue()))).collect(Collectors.toList());
    }
    
    /**
     * Create a list of ComboAlias objects from an array of names and aliases. The aliases need not be Strings.
     * The toString() method will be called on them to get a String representation.
     * @param names an array of names
     * @param values an array of aliases.
     * @return a List of ComboAlias objects
     * @throws ArrayIndexOutOfBoundsException if names.length != values.length
     */
    public static List<ComboAlias> fromArray(String[] names, Object[] values) {
        if (names.length != values.length)
            throw new ArrayIndexOutOfBoundsException("Name/Alias array lengths must be the same");
        
        List<ComboAlias> results = new ArrayList<ComboAlias>(names.length + 2);
        for (int x = 0; x < names.length; x++)
            results.add(new ComboAliasImpl(names[x], String.valueOf(values[x])));

        return results;
    }
}