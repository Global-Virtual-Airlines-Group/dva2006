// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;

import org.deltava.beans.schedule.*;
import org.deltava.util.CollectionUtils;

/**
 * A utility class to filter Accomplishments.
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

class AccomplishmentFilter {

	// Static class
	private AccomplishmentFilter() {
		super();
	}
	
	/**
	 * Helper method to determine whether an object is included in an Accomplishment. 
	 */
	private static <T> boolean matches(T entry, Accomplishment a) {
		if (entry instanceof Airport) {
			Airport ap = (Airport) entry;
			return ((a.getChoices().contains(ap.getIATA()) || (a.getChoices().contains(ap.getICAO()))));
		} else if (entry instanceof State) {
			State st = (State) entry;
			return (a.getChoices().contains(st.name()));
		} else if (entry instanceof Country) {
			Country c = (Country) entry;
			return (a.getChoices().contains(c.getCode()));
		} else if (entry instanceof Airline) {
			Airline al = (Airline) entry;
			return (a.getChoices().contains(al.getCode()));
		}

		return (a.getChoices().contains(entry.toString()));
	}
	
	/**
	 * Filters values to those listed as an Accomplishment's choices.
	 * @param values the values
	 * @param a the Accomplishment bean
	 * @return a Collection of values
	 * @see Accomplishment#getChoices()
	 */
	static <T> Collection<T> filter(Collection<T> values, Accomplishment a) {
		if (a.getChoices().isEmpty())
			return new ArrayList<T>(values);
		
		Collection<T> results = new ArrayList<T>(values.size());
		for (T entry : values) {
			if (matches(entry, a))
				results.add(entry);
		}
		
		return results;
	}
	
	/**
	 * Helper method to list missing values for an Accomplishment.
	 * @param values the values
	 * @param a the Accomplishment bean
	 * @return a Collection of missing values
	 * @see Accomplishment#getChoices()
	 */
	static <T> Collection<String> missing(Collection<T> values, Accomplishment a) {
		if (a.getChoices().isEmpty())
			return Collections.emptySet();
		
		Collection<String> results = new ArrayList<String>();
		for (T entry : values) {
			if (matches(entry, a)) {
				if (entry instanceof Country)
					results.add(((Country) entry).getCode());
				else if (entry instanceof Airport)
					results.add(((Airport) entry).getIATA());
				else if (entry instanceof State)
					results.add(((State) entry).toString());
				else if (entry instanceof Airline)
					results.add(((Airline) entry).getCode());
				else
					results.add(String.valueOf(entry));
			}
		}
		
		return CollectionUtils.getDelta(a.getChoices(), results);
	}
}