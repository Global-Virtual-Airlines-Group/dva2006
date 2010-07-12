// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.functions;

import java.util.*;

import org.deltava.beans.stats.Accomplishment;

/**
 * A utility class to filter out Accomplishments so that only the Accomplishment
 * with the highest value for a given unit remains.
 * @author Luke
 * @version 3.2
 * @since 3.2
 */

public class AccomplishmentFunctions {

	/**
	 * Filters a set of Accomoplishments.
	 * @param accs a Collection of Accomplishment beans
	 * @return a Collection of Accomplishments
	 */
	public static Collection<Accomplishment> filter(Collection<Accomplishment> accs) {
		if (accs == null)
			return Collections.emptyList();
		
		Map<Accomplishment.Unit, Accomplishment> results = new HashMap<Accomplishment.Unit, Accomplishment>();
		for (Accomplishment a : accs) {
			Accomplishment a2 = results.get(a.getUnit());
			if ((a2 == null) || (a.getValue() >= a2.getValue()))
				results.put(a.getUnit(), a);
		}
		
		return new TreeSet<Accomplishment>(results.values());
	}
}