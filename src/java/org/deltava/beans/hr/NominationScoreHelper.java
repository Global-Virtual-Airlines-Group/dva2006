// Copyright 2010, 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.hr;

import java.util.*;

import org.deltava.beans.*;

/**
 * A helper class to score Nominations.
 * @author Luke
 * @version 5.0
 * @since 3.3
 */

@Helper(Nomination.class)
public final class NominationScoreHelper {
	
	private final Nomination _n;
	private final Map<Integer, Pilot> _authors = new HashMap<Integer, Pilot>();

	/**
	 * Creates the helper
	 * @param n the Nomination
	 */
	public NominationScoreHelper(Nomination n) {
		super();
		_n = n;
	}

	/**
	 * Returns the database IDs of all comment authors.
	 * @return a Collection of database IDs
	 */
	public Collection<Integer> getAuthorIDs() {
		Collection<Integer> results = new HashSet<Integer>();
		for (NominationComment nc : _n.getComments())
			results.add(Integer.valueOf(nc.getAuthorID()));
		
		return results;
	}
	
	/**
	 * Adds comment authors.
	 * @param authors a Collection of Pilots
	 */
	public void addAuthors(Collection<Pilot> authors) {
		for (Pilot p : authors)
			_authors.put(Integer.valueOf(p.getID()), p);
	}
	
	/**
	 * Returns the Nomination score.
	 * @return the score
	 */
	public int getScore() {
		int total = 0;
		for (NominationComment nc : _n.getComments()) {
			Pilot author = _authors.get(Integer.valueOf(nc.getAuthorID()));
			if (author == null)
				continue;

			int score = 5;
			if (author.getID() == _n.getID())
				score = 1;
			else if (author.isInRole("HR"))
				score = 30;
			else if (Rank.SC == author.getRank())
				score = 10;
			else if (author.getRank().isCP())
				score = 15;
			
			if (nc.getSupport())
				total += score;
			else
				total -= score;
		}
		
		return Math.max(0, total);
	}
}