// Copyright 2006, 2007, 2009, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import java.util.*;

/**
 * A bean to store multiple-choice examination questions.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class MultiChoiceQuestion extends Question implements MultipleChoice {

	private final Collection<String> _choices = new LinkedHashSet<String>();
	
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
	 * @return a List of choices
	 * @see MultiChoiceQuestionProfile#getChoices()
	 */
	@Override
	public List<String> getChoices() {
		return new ArrayList<String>(_choices);
	}
	
	/**
	 * Returns the maximum length of any of the answer choices.
	 * @return the maximum answer length
	 */
	public int getMaxAnswerLength() {
		OptionalInt i = _choices.stream().mapToInt(String::length).max();
		return i.orElse(0);
	}

	/**
	 * Adds a choice to the available choices.
	 * @param choice the choice text
	 * @throws NullPointerException if choice is null
	 * @see MultiChoiceQuestion#getChoices()
	 * @see MultiChoiceQuestionProfile#addChoice(String)
	 */
	@Override
	public void addChoice(String choice) {
		String tmp = choice.trim().replace("\n", "");
		_choices.add(tmp.replace("\r", ""));
	}
	
    /**
     * Returns if the user has provided the exact correct answer.
     * @return FALSE always
     */
	@Override
	public final boolean getExactMatch() {
		return false;
	}
}