// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import java.util.Date;

import org.deltava.beans.*;
import org.deltava.beans.system.AirlineInformation;

/**
 * An class to implement commonalities between user examinations and flight videos.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class Test extends DatabaseBean implements ViewEntry {

    public static final int EXAM = 0;
    public static final int VIDEO = 1;
    public static final int CHECKRIDE = 2;
    
    /**
     * Examination type names.
     */
    public static final String[] EXAMTYPES = {"Examination", "Flight Video", "Check Ride"};
    
    public static final int NEW = 0;
    public static final int SUBMITTED = 1;
    public static final int SCORED = 2;
    
    /**
     * Examination status names.
     */
    public static final String[] EXAMSTATUS = {"New", "Submitted", "OK"};
    
    private String _name;
    private int _status;
    private int _pilotID;
    private int _scorerID;
    
    private Date _createdOn;
    private Date _submittedOn;
    private Date _scoredOn;
    
    private String _firstName;
    private String _lastName;

    protected int _score;

    private int _stage;
    private boolean _pass;
    private boolean _academy;
    
    private AirlineInformation _owner;
    private String _comments;
    
    /**
     * Returns the type of examination.
     * @return the examination type
     * @see Test#EXAMTYPES
     */
    public abstract int getType();

    /**
     * Create a new examination/video with a particular name.
     * @param name the name of the exam/video
     * @throws NullPointerException if name is null
     * @see Test#getName()
     */
    protected Test(String name) {
        super();
        _name = name.trim();
        _createdOn = new Date();
    }
    
    /**
     * Returns the name of the examination/video.
     * @return the name of the exam/video
     */
    public String getName() {
        return _name;
    }
    
    /**
     * Returns the Pilot's first (given) name.
     * @return the Pilot's first name
     * @see Test#setFirstName(String)
     */
    public String getFirstName() {
      return _firstName;  
    }
    
    /**
     * Returns the Pilot's last (family) name.
     * @return the Pilot's last name
     * @see Test#setLastName(String)
     */
    public String getLastName() {
        return _lastName;
    }
    
    /**
     * Returns the Pilot's Database ID. This corresponds to the key in the <i>PILOTS</i> for the entry of the Pilot.
     * @return the database ID
     * @see Test#setPilotID(int)
     */
    public int getPilotID() {
        return _pilotID;
    }
    
    /**
     * Returns the status of this examination.
     * @return the status code
     * @see Test#setStatus(int)
     * @see Test#setStatus(String)
     * @see Test#getStatusName()
     */
    public int getStatus() {
        return _status;
    }
    
    /**
     * Returns the status description for this examination.
     * @return the status description
     * @see Test#getStatus()
     */
    public String getStatusName() {
    	return EXAMSTATUS[_status]; 
    }
    
    /**
     * Returns whether this Test is part of the Flight Academy.
     * @return TRUE if the Test is part of the Flight Academy, otherwise FALSE
     * @see Test#setAcademy(boolean)
     */
    public boolean getAcademy() {
    	return _academy;
    }
    
    /**
     * Returns the owner Airline of this Examination.
     * @return the AirlineInformation bean
     * @see Test#setOwner(AirlineInformation)
     */
    public AirlineInformation getOwner() {
    	return _owner;
    }
    
    /**
     * Returns the Scorer's Database ID. This corresponds to the key in the <i>PILOTS</i> for the entry of the Pilot
     * who scored this Test.
     * @return the database ID
     * @see Test#setScorerID(int)
     */
    public int getScorerID() {
        return _scorerID;
    }
    
    /**
     * Returns the score of this examination.
     * @return the score achieved, from 0 to 100.
     * @see Test#setScore(int)
     */
    public int getScore() {
        return _score;
    }
    
    /**
     * Returns the number of questions in the examination.
     * @return the size of the examination.
     */
    public abstract int getSize();
    
    /**
     * Returns the stage level of this examination/checkride.
     * @return the stage from 1 to 5
     * @see Test#setStage(int)
     */
    public int getStage() {
        return _stage;
    }
    
    /**
     * Returns if the Pilot passed or failed this examination
     * @return TRUE if the pilot passed, otherwise FALSE
     * @see Test#setPassFail(boolean)
     */
    public boolean getPassFail() {
        return _pass;
    }
    
    /**
     * Returns the Examination comments.
     * @return the comments
     * @see Test#setComments(String)
     */
    public String getComments() {
    	return _comments;
    }
    
    /**
     * Returns the date this Examination was created/performed on.
     * @return the creation Date
     * @see Test#setDate(Date)
     */
    public Date getDate() {
        return _createdOn;
    }
    
    /**
     * Returns the date/time this Examination was submitted on.
     * @return the submission Date
     * @see Test#setSubmittedOn(Date)
     */
    public Date getSubmittedOn() {
        return _submittedOn;
    }
    
    /**
     * Returns the date/time this Examination was scored on
     * @return the scoring Date
     * @see Test#setScoredOn(Date)
     */
    public Date getScoredOn() {
        return _scoredOn;
    }
    
    /**
     * Sets the first (given) name of the Pilot.
     * @param name the Pilot's first name
     * @see Test#getFirstName()
     */
    public void setFirstName(String name) {
        _firstName = name.trim();
    }
    
    /**
     * Sets the last (family) name of the Pilot.
     * @param name the Pilot's last name
     * @see Test#getLastName()
     */
    public void setLastName(String name) {
        _lastName = name.trim();
    }
    
    /**
     * Updates the database ID for the Pilot. <i>This will typically be called by a DAO</i>.
     * @param id the new database ID
     * @throws IllegalArgumentException if id is zero or negative
     * @see Test#getPilotID()
     * @see DatabaseBean#validateID(int, int)
     */
    public void setPilotID(int id) {
        validateID(_pilotID, id);
        _pilotID = id;
    }
    
    /**
     * Marks this Test as part of the Flight Academy.
     * @param academy TRUE if the Test is part of the Flight Academy, otherwise FALSE
     * @see Test#getAcademy()
     */
    public void setAcademy(boolean academy) {
    	_academy = academy;
    }
    
    /**
     * Updates the database ID for the Pilot who scored this exam. <i>This will typically be called by a DAO</i>.
     * @param id the new database ID
     * @throws IllegalArgumentException if id is negative
     * @see Test#getScorerID()
     */
    public void setScorerID(int id) {
    	if (id < 0)
    		throw new IllegalArgumentException("ID cannot be negative");

        _scorerID = id;
    }
    
    /**
     * Sets the score for this Examination.
     * @param score the number of correct answers
     * @throws IllegalArgumentException if score is negative or greater than getSize()
     * @see Test#getScore()
     */
    public void setScore(int score) {
        if ((score < 0) || (score > getSize()))
            throw new IllegalArgumentException("Score cannot be < 0 or > " + getSize());
        
        _score = score;
    }
    
    /**
     * Sets the status for this Examination.
     * @param status the status code
     * @throws IllegalArgumentException if status is negative or invalid
     * @see Test#setStatus(String)
     * @see Test#getStatus()
     */
    public void setStatus(int status) {
        if ((status < 0) || (status > Test.EXAMSTATUS.length))
            throw new IllegalArgumentException("Invalid Status code - " + status);
        
        _status = status;
    }

    /**
     * Sets the status for this Examination.
     * @param statusName the status name
     * @throws IllegalArgumentException if status name is invalid
     * @see Test#setStatus(int)
     * @see Test#getStatus()
     */
    public void setStatus(String statusName) {
        for (int x = 0; x < Test.EXAMSTATUS.length; x++) {
            if (Test.EXAMSTATUS[x].equalsIgnoreCase(statusName)) {
                _status = x;
                return;
            }
        }
        
        throw new IllegalArgumentException("Invalid Status name - " + statusName);
    }
    
    /**
     * Sets the stage level for this Examination.
     * @param stage the stage
     * @throws IllegalArgumentException if stage is zero or negative
     * @see Test#getStage()
     */
    public void setStage(int stage) {
        if (stage < 1)
            throw new IllegalArgumentException("Stage cannot be zero or negative");
        
        _stage = stage;
    }
    
    /**
     * Sets wether this Pilot passed or failed the examination.
     * @param passFail TRUE if the Pilot passed, otherwise FALSE
     * @see Test#getPassFail()
     */
    public void setPassFail(boolean passFail) {
        _pass = passFail;
    }
    
    /**
     * Updates the Examination comments.
     * @param comments the comments
     * @see Test#getComments()
     */
    public void setComments(String comments) {
    	_comments = comments;
    }
    
    /**
     * Updates the number of questions in this examination.
     * @param size the number of questions
     */
    public abstract void setSize(int size);
    
    /**
     * Updates this Examination's date.
     * @param dt the new date/time
     * @see Test#getDate()
     */
    public void setDate(Date dt) {
        _createdOn = dt;
    }
    
    /**
     * Updates this Examination's submission date.
     * @param dt the new submission date/time
     * @see Test#getSubmittedOn()
     */
    public void setSubmittedOn(Date dt) {
        _submittedOn = dt;
    }
    
    /**
     * Updates this Examination's "scored on" date.
     * @param dt the new date/time this Examination was graded on
     * @see Test#getScoredOn()
     */
    public void setScoredOn(Date dt) {
        _scoredOn = dt;
    }
    
    /**
     * Updates this Examination's owner Airline.
     * @param ai the AirlineInformation bean
     * @see Test#getOwner()
     */
    public void setOwner(AirlineInformation ai) {
    	_owner = ai;
    }
    
    /**
     * Compares examinations/checkrides by comparing their date.
     */
    public int compareTo(Object o2) {
        Test t2 = (Test) o2;
        int tmpResult = _createdOn.compareTo(t2.getDate());
        return (tmpResult == 0) ? Integer.valueOf(getID()).compareTo(Integer.valueOf(t2.getID())) : tmpResult;
    }
}