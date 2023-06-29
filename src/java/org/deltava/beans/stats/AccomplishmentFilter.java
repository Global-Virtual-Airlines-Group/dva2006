// Copyright 2010, 2020, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.util.*;
import java.util.stream.Collectors;

import org.deltava.beans.schedule.*;
import org.deltava.util.CollectionUtils;

/**
 * A utility class to filter Accomplishments.
 * @author Luke
 * @version 11.0
 * @since 3.2
 */

class AccomplishmentFilter {

	// Static class
	private AccomplishmentFilter() {
		super();
	}
	
	/**
	 * Determines whether a RoutePair updates an Accomplishment's counts, if an Accomplishment's unit uses geolocations.
	 * @param rp the RoutePair
	 * @param a the Accomplishment
	 * @return TRUE if the RoutePair's airports meet the Accomplishment criteria
	 * @see AccomplishUnit#isGeo()
	 */
	static boolean matchesGeo(RoutePair rp, Accomplishment a) {
		AccomplishUnit u = a.getUnit();
		if (!u.isGeo()) return true;
		
		switch (u) {
		case AIRPORTD:
		case ADLEGS:
			return matches(rp.getAirportD(), a);
			
		case AIRPORTA:
		case AALEGS:
			return matches(rp.getAirportA(), a);
			
		case AIRPORTS:
			return matches(rp.getAirportD(), a) || matches(rp.getAirportA(), a);
			
		case COUNTRIES:
			return matches(rp.getAirportD().getCountry(), a) || matches(rp.getAirportA().getCountry(), a);
			
		case STATES:
			return matches(rp.getAirportD().getState(), a) || matches(rp.getAirportA().getState(), a);
			
		case CONTINENTS:
			return matches(rp.getAirportD().getCountry().getContinent(), a) || matches(rp.getAirportA().getCountry().getContinent(), a);
			
		default:
			return false;
		}
	}
	
	/*
	 * Helper method to determine whether an object is included in an Accomplishment. 
	 */
	private static <T> boolean matches(T entry, Accomplishment a) {
		if (entry instanceof Airport ap)
			return a.getChoices().contains(ap.getIATA()) || a.getChoices().contains(ap.getICAO());
		else if (entry instanceof State st)
			return a.getChoices().contains(st.name());
		else if (entry instanceof Country c)
			return a.getChoices().contains(c.getCode());
		else if (entry instanceof Airline al)
			return a.getChoices().contains(al.getCode());
		else if (entry instanceof Continent c)
			return a.getChoices().contains(c.name());

		return (a.getChoices().contains(String.valueOf(entry)));
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
		
		return values.stream().filter(v -> matches(v, a)).collect(Collectors.toList());
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
				if (entry instanceof Country c)
					results.add(c.getCode());
				else if (entry instanceof Airport ap)
					results.add(ap.getIATA());
				else if (entry instanceof State s)
					results.add(s.toString());
				else if (entry instanceof Airline al)
					results.add(al.getCode());
				else if (entry instanceof Continent c)
					results.add(c.name());
				else
					results.add(String.valueOf(entry));
			}
		}
		
		return CollectionUtils.getDelta(a.getChoices(), results);
	}
}