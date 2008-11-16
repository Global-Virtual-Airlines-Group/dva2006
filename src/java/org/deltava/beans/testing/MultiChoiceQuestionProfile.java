// Copyright 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import java.util.*;

/**
 * A bean to store multiple-choice question data.
 * @author Luke
 * @version 2.3
 * @since 1.0
 */

public class MultiChoiceQuestionProfile extends QuestionProfile implements MultipleChoice {

	private Collection<String> _choices = new LinkedHashSet<String>();
	
	/**
	 * Creates a new multiple-choice question profile.
	 * @param text the Question text
	 * @throws NullPointerException if text is null
	 */
	public MultiChoiceQuestionProfile(String text) {
		super(text);
	}

	/**
	 * Returns the list of choices.
	 * @return a Collection of choices
	 * @see MultiChoiceQuestionProfile#addChoice(String)
	 * @see MultiChoiceQuestionProfile#setChoices(Collection)
	 */
	public Collection<String> getChoices() {
		return _choices;
	}
	
	/**
	 * Adds a choice to the available choices.
	 * @param choice the choice text
	 * @throws NullPointerException if choice is null
	 * @see MultiChoiceQuestionProfile#setChoices(Collection)
	 * @see MultiChoiceQuestionProfile#getChoices()
	 */
	public void addChoice(String choice) {
		String tmp = choice.trim().replace("\n", "");
		_choices.add(tmp.replace("\r", ""));
	}
	
	/**
	 * Clears and updates the list of available choices.
	 * @param choices a Collection of choices
	 * @see MultiChoiceQuestionProfile#addChoice(String)
	 * @see MultiChoiceQuestionProfile#getChoices()
	 */
	public void setChoices(Collection<String> choices) {
		_choices.clear();
		for (Iterator<String> i = choices.iterator(); i.hasNext(); )
			addChoice(i.next());
	}
	
	/**
	 * Converts this profile into a {@link MultiChoiceQuestion} bean. The choices
	 * will be rearranged in random order.
	 */
	public Question toQuestion() {
		MultiChoiceQuestion q = new MultiChoiceQuestion(getQuestion());
		q.setID(getID());
		q.setCorrectAnswer(getCorrectAnswer());
		List<String> rndChoices = new ArrayList<String>(_choices);
		Collections.shuffle(rndChoices);
		for (String c : rndChoices)
			q.addChoice(c);
		
		return q;
	}
}