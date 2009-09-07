// Copyright 2005, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.*;

import org.deltava.beans.ComboAlias;

/**
 * A utility class to generate combobox lists.
 * @author Luke
 * @version 2.6
 * @since 1.0
 * @see ComboAlias
 */

public class ComboUtils {

    private ComboUtils() { // private constructor since we are all static
    	super();
    }

    private static class ComboAliasImpl implements ComboAlias, Comparable<ComboAlias> {
        
        private String _name;
        private String _alias;
        
        ComboAliasImpl(String name, String alias) {
            super();
            _name = name;
            _alias = alias;
        }
        
        ComboAliasImpl(String name) {
            this(name, name);
        }
        
        public String getComboAlias() {
            return _alias;
        }
        
        public String getComboName() {
            return _name;
        }
        
        public int compareTo(ComboAlias c2) {
        	int tmpResult = _name.compareTo(c2.getComboName());
        	if (tmpResult == 0)
        		tmpResult = _alias.compareTo(c2.getComboAlias());
        	
        	return tmpResult;
        }
        
        public boolean equals(Object o2) {
        	return (o2 instanceof ComboAlias) ? (compareTo((ComboAlias) o2) == 0) : false;
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
     * Create a list of ComboAlias objects from a List of strings. The name/alias will be the same.
     * @param names a List of names
     * @return a List of ComboAlias objects
     */
    public static List<ComboAlias> fromList(Collection<String> names) {
        List<ComboAlias> results = new ArrayList<ComboAlias>(names.size());
        for (Iterator<String> i = names.iterator(); i.hasNext(); )
            results.add(new ComboAliasImpl(i.next()));
        
        return results;
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
        List<ComboAlias> results = new ArrayList<ComboAlias>(names.size());
        for (Iterator<String> i = names.keySet().iterator(); i.hasNext(); ) {
            String name = i.next();
            results.add(new ComboAliasImpl(name, names.get(name).toString()));
        }
        
        return results;
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
        
        List<ComboAlias> results = new ArrayList<ComboAlias>(names.length);
        for (int x = 0; x < names.length; x++)
            results.add(new ComboAliasImpl(names[x], values[x].toString()));

        return results;
    }
    
    /**
     * Checks through a list of ComboAlias objects and returns the name matching a given alias.
     * @param choices the List of ComboAlias objects
     * @param alias the alias to search for
     * @return the first name matching the alias, or null if not found
     * @see ComboUtils#getAlias(Collection, String)
     */
    public static String getName(Collection<ComboAlias> choices, String alias) {
        for (Iterator<ComboAlias> i = choices.iterator(); i.hasNext(); ) {
            ComboAlias ca = i.next();
            if (ca.getComboAlias().equals(alias))
                return ca.getComboName();
        }
        
        return null;
    }
    
    /**
     * Checks through a collection of ComboAlias objects and returns the alias matching a given name.
     * @param choices a Collection of ComboAlias objects
     * @param name the name to search for
     * @return the first alias matching the name, or null if not found
     * @see ComboUtils#getName(Collection, String)
     */
    public static String getAlias(Collection<ComboAlias> choices, String name) {
        for (Iterator<ComboAlias> i = choices.iterator(); i.hasNext(); ) {
            ComboAlias ca = i.next();
            if (ca.getComboName().equals(name))
                return ca.getComboAlias();
        }
        
        return null;
    }
}