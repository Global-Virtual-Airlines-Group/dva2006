// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import java.util.*;

/**
 * A bean to store multiple-choice examination questions.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MultiChoiceQuestion extends Question implements MultipleChoice {

	private Collection<String> _choices = new LinkedHashSet<String>();
	
	/**
	 * Creates a new Question bean
	 * @param text the Question text
	 * @throws NullPointerException if text is null
	 */
	public MultiChoiceQuestion(String text) {
		super(text);
	}

	/**
	 * Returns the list of choices.
	 * @return a Collection of choices
	 * @see MultiChoiceQuestionProfile#getChoices()
	 */
	public Collection<String> getChoices() {
		return _choices;
	}

	/**
	 * Adds a choice to the available choices.
	 * @param choice the choice text
	 * @throws NullPointerException if choice is null
	 * @see MultiChoiceQuestion#getChoices()
	 * @see MultiChoiceQuestionProfile#addChoice(String)
	 */
	public void addChoice(String choice) {
		String tmp = choice.trim().replace("\n", "");
		_choices.add(tmp.replace("\r", ""));
	}
	
    /**
     * Returns if the user has provided the exact correct answer.
     * @return FALSE always
     */
	public final boolean getExactMatch() {
		return false;
	}
}