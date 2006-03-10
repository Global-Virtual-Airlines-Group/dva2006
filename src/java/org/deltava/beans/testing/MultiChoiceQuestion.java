// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import java.util.*;

/**
 * A bean to store multiple-choice examination questions.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class MultiChoiceQuestion extends Question {

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
	 * Creates a new Question from an existing Question Profile.
	 * @param qp the multiple choice Question Profile
	 */
	public MultiChoiceQuestion(MultiChoiceQuestionProfile qp) {
		super(qp);
		_choices.addAll(qp.getChoices());
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
		_choices.add(choice.trim());
	}
}