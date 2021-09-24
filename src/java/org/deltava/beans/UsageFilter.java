// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A utility class to filter beans with usage counts.
 * @param <T> the UseCount class
 * @author Luke
 * @version 10.2
 * @since 10.2
 * @see UseCount
 */

public interface UsageFilter<T extends UseCount> {
	
	/**
	 * Filters an individual bean. 
	 * @param uc the UseCount bean
	 * @param max the maximum usage per bean within this Collection
	 * @param total the total bean usage within this Collection
	 * @return TRUE if the bean should be included, otherwise FALSE
	 */
	public boolean filter(T uc, int max, int total); 

	/**
	 * Filters a Collection of UseCount beans.
	 * @param data the beans to filter
	 * @return a filtered List of beans that met the criteria
	 */
	default List<T> filter(Collection<T> data) {
		final int max = data.stream().mapToInt(UseCount::getUseCount).max().orElse(0);
		final int total = data.stream().mapToInt(UseCount::getUseCount).sum();
		return data.stream().filter(d -> filter(d, max, total)).collect(Collectors.toList());
	}
}