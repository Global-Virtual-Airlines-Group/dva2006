// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.hr;

import java.util.*;

import org.deltava.beans.Pilot;
import org.deltava.beans.Rank;

/**
 * A helper class to score Nominations.
 * @author Luke
 * @version 3.3
 * @since 3.3
 */

public class NominationScoreHelper {
	
	private Nomination _n;
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
			results.add(new Integer(nc.getAuthorID()));
		
		return results;
	}
	
	/**
	 * Adds comment authors.
	 * @param authors a Collection of Pilots
	 */
	public void addAuthors(Collection<Pilot> authors) {
		for (Pilot p : authors)
			_authors.put(new Integer(p.getID()), p);
	}
	
	/**
	 * Returns the Nomination score.
	 * @return the score
	 */
	public int getScore() {
		int score = 0;
		for (NominationComment nc : _n.getComments()) {
			Pilot author = _authors.get(new Integer(nc.getAuthorID()));
			if (author == null)
				continue;
			
			if (author.getID() == _n.getID())
				score += 1;
			else if (author.hasRating("HR"))
				score += 30;
			else if (Rank.SC == author.getRank())
				score += 10;
			else if (author.getRank().isCP())
				score += 15;
			else
				score += 5;
		}
		
		return score;
	}
}