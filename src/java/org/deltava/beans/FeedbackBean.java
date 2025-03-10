// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.Collection;

/**
 * An interface for beans that collect user feedback. 
 * @author Luke
 * @version 11.6
 * @since 11.6
 */

public interface FeedbackBean extends IDBean {
	
	/**
	 * Returns the feedback for this bean.
	 * @return a Collection of Feedback beans
	 */
	public Collection<Feedback> getFeedback();

	/**
	 * Adds a feedback element to the bean.
	 * @param f a Feedback object
	 */
	public void addFeedback(Feedback f);

	/**
	 * Returns whether feedback has been provided by a particular user.
	 * @param authorID the User's database ID
	 * @return TRUE if the User has provided feedback, otherwise FALSE
	 */
	default boolean hasFeedback(int authorID) {
		return getFeedback().stream().anyMatch(f -> f.getAuthorID() == authorID);
	}
}