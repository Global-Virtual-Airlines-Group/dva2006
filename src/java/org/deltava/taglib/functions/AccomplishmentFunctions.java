// Copyright 2010, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.functions;

import java.util.*;

import org.deltava.beans.stats.*;

/**
 * A utility class to filter out Accomplishments so that only the Accomplishment
 * with the highest value for a given unit remains.
 * @author Luke
 * @version 6.3
 * @since 3.2
 */

public class AccomplishmentFunctions {

	/**
	 * Filters a set of Accomoplishments.
	 * @param accs a Collection of Accomplishment beans
	 * @return a Collection of Accomplishments
	 * @see Accomplishment#getAlwaysDisplay()
	 */
	public static Collection<Accomplishment> filter(Collection<Accomplishment> accs) {
		if (accs == null)
			return Collections.emptyList();
		
		Collection<Accomplishment> allResults = new TreeSet<Accomplishment>();
		Map<AccomplishUnit, Accomplishment> results = new HashMap<AccomplishUnit, Accomplishment>();
		for (Accomplishment a : accs) {
			if (a.getAlwaysDisplay()) {
				allResults.add(a);
				continue;
			}
			
			Accomplishment a2 = results.get(a.getUnit());
			if ((a2 == null) || (a.getValue() >= a2.getValue()))
				results.put(a.getUnit(), a);
		}
		
		allResults.addAll(results.values());
		return allResults;
	}
}