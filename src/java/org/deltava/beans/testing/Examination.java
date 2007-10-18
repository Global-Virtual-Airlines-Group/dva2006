// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import java.util.*;

import org.deltava.util.StringUtils;

/**
 * A class to store information about written examinations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Examination extends Test {

	/**
	 * Applicant Questionnaire Examination Name.
	 */
	public static final String QUESTIONNAIRE_NAME = "Questionnaire";

	private static final String[] CLASS_NAMES = { "opt2", "opt1", null };

	private Date _expiryDate;
	private Map<Integer, Question> _questions;
	private int _size;
	private boolean _empty;
	private boolean _autoScored;

	/**
	 * Creates a new examination.
	 * @param name the name of the examination
	 */
	public Examination(String name) {
		super(name);
		_questions = new TreeMap<Integer, Question>();
	}

	/**
	 * Returns the number of questions in this Examination.
	 * @return the number of questions
	 * @see Examination#setSize(int)
	 */
	public int getSize() {
		return (_questions.size() == 0) ? _size : _questions.size();
	}

	/**
	 * Returns the Expiration Date of this Examination.
	 * @return the date/time this exam must be completed by
	 * @see Examination#setExpiryDate(Date)
	 */
	public Date getExpiryDate() {
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
	 * Returns wether this Examination's answers were empty.
	 * @return TRUE if all Answers are blank, otherwise FALSE
	 * @see Examination#setEmpty(boolean)
	 */
	public boolean getEmpty() {
		if (_questions.isEmpty())
			return _empty;

		// Check the question answers
		for (Iterator<Question> i = _questions.values().iterator(); i.hasNext();) {
			Question q = i.next();
			if (!StringUtils.isEmpty(q.getAnswer()))
				return false;
		}

		return true;
	}

	/**
	 * Returns a specific Question from the Examination.
	 * @param idx the question number
	 * @return the Question with the specified number, or null if not present
	 */
	public Question getQuestion(int idx) {
		return _questions.get(new Integer(idx));
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
	 * Returns the test type.
	 * @return always Text.EXAM
	 * @see Test#getType()
	 */
	public int getType() {
		return Test.EXAM;
	}
	
	/**
	 * Returns if the Examination has any multiple-choice questions.
	 * @return TRUE if there is at least one multiple-choice question, otherwise FALSE
	 */
	public boolean hasMultipleChoice() {
		for (Iterator<Question> i = _questions.values().iterator(); i.hasNext(); ) {
			Question q = i.next();
			if (q instanceof MultipleChoice)
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns if the Examination has any questions with images.
	 * @return TRUE if there is at least one question with an image, otherwise false
	 */
	public boolean hasImage() {
		for (Iterator<Question> i = _questions.values().iterator(); i.hasNext(); ) {
			Question q = i.next();
			if (q.getSize() > 0)
				return true;
		}
		
		return false;
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
		_questions.put(new Integer(qNum), q);
		q.setNumber(qNum);
	}

	/**
	 * Updates the size of this examination.
	 * @param size the number of questions
	 * @throws IllegalStateException if at least one question has been aded to this exam
	 * @throws IllegalArgumentException if size is zero or negative
	 * @see Examination#getSize()
	 */
	public void setSize(int size) {
		if (_questions.size() != 0)
			throw new IllegalStateException("Question Pool already populated");
		else if (size < 1)
			throw new IllegalArgumentException("Examination Size cannot be zero or negative");

		_size = size;
	}

	/**
	 * Updates the expiration date of this Examination.
	 * @param dt the new expiration date
	 * @see Examination#getExpiryDate()
	 */
	public void setExpiryDate(Date dt) {
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
	public String getRowClassName() {
		if (getEmpty() && (getStatus() == Test.SCORED))
			return "warn";

		return CLASS_NAMES[getStatus()];
	}
}