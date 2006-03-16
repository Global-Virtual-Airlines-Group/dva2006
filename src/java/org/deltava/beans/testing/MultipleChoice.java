// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import java.util.Collection;

/**
 * An interface to mark Multiple-Choice questions and question profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface MultipleChoice {

	/**
	 * Adds a choice to the list of choices for this question.
	 * @param choice the text
	 */
	public void addChoice(String choice);
		
	/**
	 * Returns the available answer choices for this question.
	 * @return a Collection of choice Strings
	 */
	public Collection<String> getChoices();
}