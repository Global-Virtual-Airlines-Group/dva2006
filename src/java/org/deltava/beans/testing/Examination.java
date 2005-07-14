package org.deltava.beans.testing;

import java.util.*;

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
   public static final String QUESTIONNAIRE_NAME = "Initial Questionnaire";

    private Date _expiryDate;
    private List _questions;
    private int _size;
    
    /**
     * Creates a new examination.
     * @param name the name of the examination
     */
    public Examination(String name) {
        super(name);
        _questions = new ArrayList();
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
    public List getQuestions() {
        return _questions;
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
     * Adds a Question to the Examination.
     * @param q the Question to add
     * @see Examination#getQuestions()
     */
    public void addQuestion(Question q) {
        _questions.add(q);
        q.setNumber(_questions.size());
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
        
        if (size < 1)
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
}