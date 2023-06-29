// Copyright 2005, 2006, 2007, 2008, 2012, 2016, 2017, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import java.util.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.deltava.util.StringUtils;

/**
 * A class to store information about written examinations.
 * @author Luke
 * @version 11.0
 * @since 1.0
 */

public class Examination extends Test {

	/**
	 * Applicant Questionnaire Examination Name.
	 */
	public static final String QUESTIONNAIRE_NAME = "Questionnaire";

	private static final String[] CLASS_NAMES = { "opt2", "opt1", null };

	private Instant _expiryDate;
	private final Map<Integer, Question> _questions = new TreeMap<Integer, Question>();
	private int _size;
	private boolean _empty;
	private boolean _autoScored;

	/**
	 * Creates a new examination.
	 * @param name the name of the examination
	 */
	public Examination(String name) {
		super(name);
	}

	/**
	 * Returns the number of questions in this Examination.
	 * @return the number of questions
	 * @see Examination#setSize(int)
	 */
	@Override
	public int getSize() {
		return (_questions.size() == 0) ? _size : _questions.size();
	}

	/**
	 * Returns the Expiration Date of this Examination.
	 * @return the date/time this exam must be completed by
	 * @see Examination#setExpiryDate(Instant)
	 */
	public Instant getExpiryDate() {
		return _expiryDate;
	}

	/**
	 * Returns this Examination's questions.
	 * @return a List of questions.
	 * @see Question
	 * @see Examination#addQuestion(Question)
	 */
	public Collection<Question> getQuestions() {
		return _questions.values();
	}

	/**
	 * Returns whether this Examination's answers were empty.
	 * @return TRUE if all Answers are blank, otherwise FALSE
	 * @see Examination#setEmpty(boolean)
	 */
	public boolean getEmpty() {
		if (_questions.isEmpty())
			return _empty;

		for (Question q : _questions.values()) {
			if (!StringUtils.isEmpty(q.getAnswer()))
				return false;
		}

		return true;
	}
	
	/**
	 * Returns if a route plotting question is included in this Examination. 
	 * @return TRUE if a route plotting question is included, otherwise FALSE
	 * @see RoutePlot
	 */
	public boolean getRoutePlot() {
		return _questions.values().stream().anyMatch(RoutePlot.class::isInstance);
	}

	/**
	 * Returns a specific Question from the Examination.
	 * @param idx the question number
	 * @return the Question with the specified number, or null if not present
	 */
	public Question getQuestion(int idx) {
		return _questions.get(Integer.valueOf(idx));
	}
	
	/**
	 * Returns whether this Examination was automatically scored.
	 * @return TRUE if the Examination was automatically scored, otherwise FALSE
	 * @see Examination#getAutoScored()
	 */
	public boolean getAutoScored() {
		return _autoScored;
	}

	/**
	 * Returns if the Examination has any multiple-choice questions.
	 * @return TRUE if there is at least one multiple-choice question, otherwise FALSE
	 */
	public boolean hasMultipleChoice() {
		return _questions.values().stream().anyMatch(MultipleChoice.class::isInstance);
	}
	
	/**
	 * Returns if the Examination has any questions with images.
	 * @return TRUE if there is at least one question with an image, otherwise false
	 */
	public boolean hasImage() {
		return _questions.values().stream().anyMatch(q -> (q.getSize() > 0));
	}

	/**
	 * Marks this Examination as having all blank answers.
	 * @param isEmpty TRUE if all answers are blank, otherwise FALSE
	 * @throws IllegalStateException if Questions have been populated
	 * @see Examination#getEmpty()
	 */
	public void setEmpty(boolean isEmpty) {
		if (!_questions.isEmpty())
			throw new IllegalStateException("Questions already loaded");

		_empty = isEmpty;
	}

	/**
	 * Adds a Question to the Examination.
	 * @param q the Question to add
	 * @see Examination#getQuestions()
	 */
	public void addQuestion(Question q) {
		// Generate a question number if the question does not currently have one
		int qNum = (q.getNumber() == 0) ? (_questions.size() + 1) : q.getNumber();
		_questions.put(Integer.valueOf(qNum), q);
		q.setNumber(qNum);
	}

	/**
	 * Updates the size of this examination.
	 * @param size the number of questions
	 * @throws IllegalStateException if at least one question has been aded to this exam
	 * @see Examination#getSize()
	 */
	@Override
	public void setSize(int size) {
		if (_questions.size() != 0)
			throw new IllegalStateException("Question Pool already populated");

		_size = size;
	}

	/**
	 * Updates the expiration date of this Examination.
	 * @param dt the new expiration date
	 * @see Examination#getExpiryDate()
	 */
	public void setExpiryDate(Instant dt) {
		_expiryDate = dt;
	}
	
	/**
	 * Updates whether this Examination was automatically scored.
	 * @param isAutoScored TRUE if the Examination was automatically scored, otherwise FALSE
	 * @see Examination#getAutoScored()
	 */
	public void setAutoScored(boolean isAutoScored) {
		_autoScored = isAutoScored;
	}

	/**
	 * Returns the CSS class name for a view table row.
	 * @return the CSS class name
	 */
	@Override
	public String getRowClassName() {
		if (getEmpty() && (getStatus() == TestStatus.SCORED))
			return "warn";
		else if ((_expiryDate != null) && (_expiryDate.isBefore(Instant.now().plus(60, ChronoUnit.DAYS))))
				return "opt3";

		return CLASS_NAMES[getStatus().ordinal()];
	}
}