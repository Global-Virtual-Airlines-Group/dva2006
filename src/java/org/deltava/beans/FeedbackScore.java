// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.*;

import org.deltava.util.cache.Cacheable;

/**
 * A bean to store aggregated feedback.
 * @author Luke
 * @version 11.6
 * @since 11.6
 */

public class FeedbackScore implements Cacheable {
	
	private final String _type;
	private final int _id;
	private final int _count;
	private final double _avg;

	/**
	 * Creates the bean.
	 * @param the database ID of the parent object
	 * @param cnt the number of feedback entries
	 * @param avg the average feedback score 
	 */
	private FeedbackScore(String type, int id, int cnt, double avg) {
		super();
		_type = type;
		_id = id;
		_count = cnt;
		_avg = avg;
	}
	
	/**
	 * Returns the database ID of the parent object.
	 * @return the database ID
	 */
	public int getID() {
		return _id;
	}
	
	/**
	 * Returns the type of the parent object.
	 * @return the type
	 */
	public String getType() {
		return _type;
	}
	
	/**
	 * Returns the number of feedback entries.
	 * @return the number of entries
	 */
	public int getSize() {
		return _count;
	}
	
	/**
	 * Returns the average feedback score.
	 * @return the average score
	 */
	public double getAverage() {
		return _avg;
	}
	
	@Override
	public String toString() {
		return String.format("%s-%d", _type, Integer.valueOf(_id));
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public Object cacheKey() {
		return toString();
	}

	/**
	 * Generates a feedback score from a FeedbackBean.
	 * @param fb a FeedbackBean
	 * @return a FeedbackScore, or null if fb is empty
	 */
	public static FeedbackScore generate(FeedbackBean fb) {
		Collection<Feedback> fc = fb.getFeedback();
		if (fc.isEmpty()) return null;
		
		// Calculate average and key
		double avg = fc.stream().mapToInt(Feedback::getScore).average().orElse(0);
		return new FeedbackScore(fb.getClass().getSimpleName(), fb.getID(), fc.size(), avg);
	}
}