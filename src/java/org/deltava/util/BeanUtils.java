// Copyright 2017, 2020, 2021, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.*;

import org.apache.commons.beanutils.*;

import org.apache.logging.log4j.*;

/**
 * A utility class to perform java bean operations.
 * @author Luke
 * @version 11.1
 * @since 7.4
 */

public class BeanUtils {
	
	private static final Logger log = LogManager.getLogger(BeanUtils.class);
	
	private static final Collection<String> DEFAULT_IGNORE_FIELDS = List.of("rowClassName", "infoBox", "class", "inputStream");

	// static class
	private BeanUtils() {
		super();
	}
	
	public static class PropertyChange implements java.io.Serializable, Comparable<PropertyChange> {
		
		private final String _name;
		private final String _old;
		private final String _new;
		
		PropertyChange(String name, String oldValue, String newValue) {
			super();
			_name = name;
			_old = oldValue;
			_new = newValue;
		}
		
		public String getName() {
			return _name;
		}
		
		@Override
		public int hashCode() {
			return _name.hashCode();
		}
		
		@Override
		public String toString() {
			return String.format("%s: \"%s\" -> \"%s\"", _name, _old, _new);
		}
		
		public String toJSON() {
			return String.format("%s: { \"%s\" -> \"%s\" }", _name, _old, _new);
		}

		@Override
		public int compareTo(PropertyChange pc) {
			return _name.compareTo(pc._name);
		}
	}
	
	/**
	 * Deep clones an object by serializing and deserializing it.
	 * @param obj the object
	 * @return a clone of the object
	 */
	public static <T extends java.io.Serializable> T clone(T obj) {
		return (obj == null) ? null : (T) IPCUtils.reserialize(obj);
	}

	/**
	 * Compares two beans and determines what fields have changed.
	 * @param o the old bean value
	 * @param n the new bean value
	 * @param ignoredFields a list of fields to ignore
	 * @return a Collection of PropertyChange beans
	 */
	public static List<PropertyChange> getDelta(Object o, Object n, String... ignoredFields) {
		if ((o == null) || (n == null)) return Collections.emptyList();
		if (!o.getClass().equals(n.getClass()))
			throw new IllegalArgumentException(String.format("Cannot compare %s to %s", o.getClass().getName(), n.getClass().getName()));
		
		BeanMap m = new BeanMap(o);
		PropertyUtilsBean pu = new PropertyUtilsBean();
		
		Collection<String> ignore = new HashSet<String>(Arrays.asList(ignoredFields));
		ignore.addAll(DEFAULT_IGNORE_FIELDS);
		List<PropertyChange> results = new ArrayList<PropertyChange>();
		for (Object pn : m.keySet()) {
			String propertyName = (String) pn;
			if (ignore.contains(propertyName)) continue;
			try {
				Object p1 = pu.getProperty(o, propertyName);
				Object p2 = pu.getProperty(n, propertyName);
				boolean dataChange = (p1 == null) ? (p1 != p2) : !p1.equals(p2);
				if (dataChange)
					results.add(new PropertyChange(propertyName, String.valueOf(p1), String.valueOf(p2)));
			} catch (Exception e) {
				log.error("{} on {}::{}", e.getMessage(), o.getClass().getSimpleName(), propertyName);
			}
		}
		
		return results;
	}
}