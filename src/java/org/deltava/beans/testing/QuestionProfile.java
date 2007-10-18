// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import java.util.*;

import org.deltava.beans.ViewEntry;
import org.deltava.beans.system.AirlineInformation;

/**
 * A class to store Exam Question profiles.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class QuestionProfile extends Question implements ViewEntry {

	private int _totalAnswers;
	private int _correctAnswers;
	private boolean _active;
	private final Collection<String> _exams = new TreeSet<String>();

	private AirlineInformation _owner;
	private final Collection<AirlineInformation> _airlines = new HashSet<AirlineInformation>();

	/**
	 * Creates a new Question Profile.
	 * @param text the question text
	 * @throws NullPointerException if text is null
	 */
	public QuestionProfile(String text) {
		super(text);
	}

	/**
	 * Returns the Examinations associated with this Question.
	 * @return a Collection of Examination names
	 * @see QuestionProfile#addExam(String)
	 * @see QuestionProfile#setExams(Collection)
	 */
	public Collection<String> getExamNames() {
		return _exams;
	}

	/**
	 * Returns the total number of times this Question has been answered correctly.
	 * @return the number of correct answers
	 * @see QuestionProfile#setCorrectAnswers(int)
	 */
	public int getCorrectAnswers() {
		return _correctAnswers;
	}

	/**
	 * Returns the total number of times this Question has been included in an Examination.
	 * @return the number of times included
	 * @see QuestionProfile#setTotalAnswers(int)
	 */
	public int getTotalAnswers() {
		return _totalAnswers;
	}

	/**
	 * Returns wether this Question is active.
	 * @return TRUE if the Question can be included in an Examination, otherwise FALSE
	 * @see QuestionProfile#setActive(boolean)
	 */
	public boolean getActive() {
		return _active;
	}

	/**
	 * Returns the Owner Airline for this Question.
	 * @return an AirlineInformation bean
	 * @see QuestionProfile#setOwner(AirlineInformation)
	 */
	public AirlineInformation getOwner() {
		return _owner;
	}

	/**
	 * Returns the Airlines that can access this Question.
	 * @return a Collection of AirlineInformation beans
	 * @see QuestionProfile#addAirline(AirlineInformation)
	 * @see QuestionProfile#setAirlines(Collection)
	 */
	public Collection<AirlineInformation> getAirlines() {
		return _airlines;
	}

	/**
	 * Sets the Question Number. <i>DISABLED</i>
	 * @throws UnsupportedOperationException always
	 */
	public final void setNumber(int number) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Updates the Question text.
	 * @param text the quesstion
	 * @throws NullPointerException if text is null
	 * @see Question#getQuestion()
	 */
	public void setQuestion(String text) {
		_text = text.trim();
	}

	/**
	 * Links this Question to an Examination.
	 * @param examName the examination name
	 * @throws NullPointerException if examName is null
	 * @see QuestionProfile#setExams(Collection)
	 * @see QuestionProfile#getExamNames()
	 */
	public void addExam(String examName) {
		_exams.add(examName.trim());
	}

	/**
	 * Links this Question to a number of Examinations.
	 * @param exams a Collection of Examination names
	 * @see QuestionProfile#addExam(String)
	 * @see QuestionProfile#getExamNames()
	 */
	public void setExams(Collection<String> exams) {
		_exams.clear();
		for (Iterator<String> i = exams.iterator(); i.hasNext();)
			addExam(i.next());
	}

	/**
	 * Marks this Question as Active.
	 * @param active TRUE if the Question can be included in an Examination, otherwise FALSE
	 * @see QuestionProfile#getActive()
	 */
	public void setActive(boolean active) {
		_active = active;
	}

	/**
	 * Updates the total number of times this Question has been answered correctly.
	 * @param count the number of correct answers
	 * @see QuestionProfile#getCorrectAnswers()
	 */
	public void setCorrectAnswers(int count) {
		if (count < 0)
			throw new IllegalArgumentException("Answers cannot be negative");

		_correctAnswers = count;
	}

	/**
	 * Updates the total number of times this Question has been included in an Examination.
	 * @param count the number of times included
	 * @see QuestionProfile#getTotalAnswers()
	 */
	public void setTotalAnswers(int count) {
		if (count < 0)
			throw new IllegalArgumentException("Answers cannot be negative");

		_totalAnswers = count;
	}
	
    /**
     * Sets which airline is the owner of this Question.
     * @param ai the AirlineInformation bean for the owner airline
     * @see QuestionProfile#getOwner()
     */
    public void setOwner(AirlineInformation ai) {
    	_owner = ai;
    	_airlines.add(ai);
    }
    
    /**
     * Makes this Question visible to an Airline.
     * @param ai the AirlineInformation bean
     * @see QuestionProfile#setAirlines(Collection)
     * @see QuestionProfile#getAirlines()
     */
    public void addAirline(AirlineInformation ai) {
    	_airlines.add(ai);
    }
    
    /**
     * Sets the Airlines that this Question will be visible to.
     * @param airlines a Collection of AirlineInformation beans
     */
    public void setAirlines(Collection<AirlineInformation> airlines) {
    	_airlines.clear();
    	_airlines.add(_owner);
    	if (airlines != null)
    		_airlines.addAll(airlines);
    }

	/**
	 * Compares this to another Question Profile by comparing their texts.
	 * @see Comparable#compareTo(Object)
	 */
	public final int compareTo(Object o2) {
		QuestionProfile qp2 = (QuestionProfile) o2;
		return getQuestion().compareTo(qp2.getQuestion());
	}

	/**
	 * Returns the CSS row class name if included in a view table.
	 * @return the CSS class name
	 */
	public String getRowClassName() {
		return _active ? null : "warn";
	}
}