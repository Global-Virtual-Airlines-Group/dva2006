// Copyright 2017, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.*;

import org.apache.commons.beanutils.*;

import org.apache.log4j.Logger;

/**
 * A utility class to perform java bean operations.
 * @author Luke
 * @version 9.0
 * @since 7.4
 */

public class BeanUtils {
	
	private static final Logger log = Logger.getLogger(BeanUtils.class);
	
	private static final Collection<String> DEFAULT_IGNORE_FIELDS = List.of("rowClassName", "infoBox", "class");

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
			StringBuilder buf = new StringBuilder(_name).append(": { \"");
			buf.append(_old);
			buf.append("\" -> \"");
			buf.append(_new);
			buf.append("\" }");
			return buf.toString();
		}

		@Override
		public int compareTo(PropertyChange pc) {
			return _name.compareTo(pc._name);
		}
	}
	
	@SuppressWarnings("unchecked")
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
			throw new IllegalArgumentException("Cannot compare " + o.getClass().getName() + " to " + n.getClass().getName());
		
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
				if (!p1.equals(p2))
					results.add(new PropertyChange(propertyName, String.valueOf(p1), String.valueOf(p2)));
			} catch (Exception e) {
				log.error(e.getMessage() + " on " + o.getClass().getSimpleName() + "::" + propertyName);
			}
		}
		
		return results;
	}
}