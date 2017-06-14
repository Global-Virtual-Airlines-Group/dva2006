// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util;

import java.util.*;

import org.apache.commons.beanutils.*;

import org.apache.log4j.Logger;

/**
 * A utility class to perform java bean operations.
 * @author Luke
 * @version 7.4
 * @since 7.4
 */

public class BeanUtils {
	
	private static final Logger log = Logger.getLogger(org.apache.axis.utils.BeanUtils.class);

	// static class
	private BeanUtils() {
		super();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T clone(T obj) {
		try {
			return (T) org.apache.commons.beanutils.BeanUtils.cloneBean(obj);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Compares two beans and determines what fields have changed.
	 * @param o the old bean value
	 * @param n the new bean value
	 * @param ignoredFields a list of fields to ignore
	 * @return a Map of old field values, keyed by property name
	 */
	public static Map<String, String> getPreviousValues(Object o, Object n, String... ignoredFields) {
		if (!o.getClass().equals(n.getClass()))
			throw new IllegalArgumentException("Cannot compare " + o.getClass().getName() + " to " + n.getClass().getName());
		
		BeanMap m = new BeanMap(o);
		PropertyUtilsBean pu = new PropertyUtilsBean();
		
		Collection<String> ignore = new HashSet<String>(Arrays.asList(ignoredFields));
		Map<String, String> results = new LinkedHashMap<String, String>();
		for (Object pn : m.keySet()) {
			String propertyName = (String) pn;
			if (ignore.contains(propertyName)) continue;
			try {
				Object p1 = pu.getProperty(o, propertyName);
				Object p2 = pu.getProperty(n, propertyName);
				if (!p1.equals(p2))
					results.put(propertyName, String.valueOf(p1));
			} catch (Exception e) {
				log.error(e.getMessage() + " on " + o.getClass().getSimpleName() + "::" + propertyName);
			}
		}
		
		return results;
	}
}