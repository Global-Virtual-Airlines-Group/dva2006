// Copyright 2006, 2007, 2008, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import java.util.*;

import org.deltava.util.StringUtils;

/**
 * A bean to store multiple-choice question data.
 * @author Luke
 * @version 3.0
 * @since 1.0
 */

public class MultiChoiceQuestionProfile extends QuestionProfile implements MultipleChoice {

	private final Collection<String> _choices = new LinkedHashSet<String>();
	
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
	public List<String> getChoices() {
		return new ArrayList<String>(_choices);
	}
	
	/**
	 * Adds a choice to the available choices.
	 * @param choice the choice text
	 * @see MultiChoiceQuestionProfile#setChoices(Collection)
	 * @see MultiChoiceQuestionProfile#getChoices()
	 */
	public void addChoice(String choice) {
		if (!StringUtils.isEmpty(choice)) {
			String tmp = choice.trim().replace("\n", "");
			_choices.add(tmp.replace("\r", ""));
		}
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
	 * Sets the correct answer to this question.
	 * @param answer the correct answer
	 */
	public void setCorrectAnswer(String answer) {
		if (!StringUtils.isEmpty(answer)) {
			String tmp = answer.trim().replace("\n", "");
			super.setCorrectAnswer(tmp.replace("\r", ""));
		}
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