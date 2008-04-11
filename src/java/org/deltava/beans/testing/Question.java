// Copyright 2005, 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import org.deltava.beans.ImageBean;

/**
 * A class to store examination question information.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class Question extends ImageBean {

	/**
	 * The question text.
	 */
    protected String _text;
    
    private String _correctAnswer;
    private String _userAnswer;
    private int _number;
    private boolean _correct;
    
    /**
     * Creates a new Question.
     * @param text the question text
     * @throws NullPointerException if text is null
     */
    public Question(String text) {
        super();
        _text = text.trim();
    }
    
    /**
     * Creates a new Question from an existing Question Profile.
     * @param qp the Question Profile
     */
    public Question(QuestionProfile qp) {
    	this(qp.getQuestion());
    	setCorrectAnswer(qp.getCorrectAnswer());
    	setID(qp.getID());
    }
    
    /**
     * Returns the Question text.
     * @return the text of the question
     * @see Question#Question(String)
     */
    public String getQuestion() {
        return _text;
    }
    
    /**
     * Returns the Correct Answer to the Question.
     * @return the correct answer
     * @see Question#setCorrectAnswer(String)
     */
    public String getCorrectAnswer() {
        return _correctAnswer;
    }
    
    /**
     * Returns the User's answer to the Question
     * @return the answer
     * @see Question#setAnswer(String)
     */
    public String getAnswer() {
        return _userAnswer;
    }
    
    /**
     * Returns the Question Number.
     * @return the question number
     * @see Question#setNumber(int)
     */
    public int getNumber() {
        return _number;
    }
    
    /**
     * Returns if the user has provided the exact correct answer. This may indicate a copy/paste.
     * @return TRUE if the answer exactly matches the correct answer
     */
    public boolean getExactMatch() {
    	return (_correctAnswer == null) ? false : _correctAnswer.equalsIgnoreCase(_userAnswer);
    }
    
    /**
     * Returns if the question was answered correctly.
     * @return TRUE if the answer is correct, otherwise FALSE
     * @see Question#setCorrect(boolean)
     */
    public boolean isCorrect() {
        return _correct;
    }
    
    /**
     * Sets the correct answer to this Question.
     * @param answer the correct answer
     * @see Question#getCorrectAnswer()
     */
    public void setCorrectAnswer(String answer) {
        _correctAnswer = answer;
    }
    
    /**
     * Sets the User-provided answer to this Question.
     * @param answer the User's answer
     * @see Question#getAnswer()
     */
    public void setAnswer(String answer) {
        _userAnswer = answer;
    }
    
    /**
     * Marks a Question as correctly answered.
     * @param isOK TRUE if the Question was answered correctly, otherwise FALSE
     * @see Question#isCorrect()
     */
    public void setCorrect(boolean isOK) {
        _correct = isOK;
    }
    
    /**
     * Updates the Question Number.
     * @param number the question number
     * @throws IllegalArgumentException if number is zero or negative
     * @see Question#getNumber()
     */
    public void setNumber(int number) {
        if (number < 1)
            throw new IllegalArgumentException("Question Number cannot be zero or negative");
        
        _number = number;
    }
    
    /**
     * Compares to another Question by comparing the Question Numbers.
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object o2) {
        Question q2 = (Question) o2;
        return new Integer(_number).compareTo(new Integer(q2.getNumber()));
    }
}