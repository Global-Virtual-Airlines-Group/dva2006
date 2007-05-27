// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import org.deltava.beans.ViewEntry;
import org.deltava.util.cache.Cacheable;

/**
 * A class to store Examination profile information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ExamProfile implements java.io.Serializable, Comparable, Cacheable, ViewEntry {

    private String _name;
    private int _stage;
    private int _questions;
    private int _passScore;
    private int _time;
    private String _eqType;
    private int _minStage;
    
    private boolean _active;
    private boolean _flightAcademy;
    
    /**
     * Creates a new Examination profile.
     * @param name the name of the examination
     * @throws NullPointerException if name is null
     * @see ExamProfile#getName()
     */
    public ExamProfile(String name) {
        super();
        setName(name);
    }
    
    /**
     * Returns the Examination Name.
     * @return the exam name
     */
    public String getName() {
        return _name;
    }
    
    /**
     * Returns the equipment program required to take this Examination.
     * @return the equipment program name
     * @see ExamProfile#setEquipmentType(String)
     */
    public String getEquipmentType() {
        return _eqType;
    }

    /**
     * Returns the stage for this Examination.
     * @return the stage
     * @see ExamProfile#setStage(int)
     */
    public int getStage() {
        return _stage;
    }
    
    /**
     * Returns the minimum stage required in order to take this Examination.
     * @return the minimum stage
     * @see ExamProfile#setMinStage(int)
     */
    public int getMinStage() {
        return _minStage;
    }
    
    /**
     * Returns the number of questions in this Examination.
     * @return the number of questions
     * @see ExamProfile#setSize(int)
     */
    public int getSize() {
        return _questions;
    }
    
    /**
     * Returns the minimum percentage required to pass this Examination.
     * @return the minimum percentage multiplied by 100
     * @see ExamProfile#setPassScore(int)
     */
    public int getPassScore() {
        return _passScore;
    }
    
    /**
     * Returns the time allowed to complete this Examination.
     * @return the time in minutes
     * @see ExamProfile#setTime(int)
     */
    public int getTime() {
        return _time;
    }
    
    /**
     * Returns wether this Examination is avialable to be taken.
     * @return TRUE if the Examination is active, otherwise FALSE
     * @see ExamProfile#setActive(boolean)
     */
    public boolean getActive() {
        return _active;
    }
    
    /**
     * Returns wther this examination is part of the Flight Academy.
     * @return TRUE if the Examination is part of the Academy, otherwise FALSE
     * @see ExamProfile#setAcademy(boolean) 
     */
    public boolean getAcademy() {
    	return _flightAcademy;
    }

    /**
     * Sets the stage for this Examination.
     * @param stage the stage number
     * @throws IllegalArgumentException if stage is zero or negative
     * @see ExamProfile#getStage()
     */
    public void setStage(int stage) {
        if (stage < 1)
            throw new IllegalArgumentException("Stage cannot be negative");
        
        _stage = stage;
    }
    
    /**
     * Updates the Examination name.
     * @param name the new name
     * @throws NullPointerException if name is null
     * @see ExamProfile#getName()
     */
    public void setName(String name) {
    	_name = name.trim();
    }
    
    /**
     * Sets the equipment program required to take this Examination.
     * @param eqType the equipment program name
     * @see ExamProfile#getEquipmentType()
     */
    public void setEquipmentType(String eqType) {
        _eqType = eqType;
    }
    
    /**
     * Sets the minimum stage required to take this Examination.
     * @param stage the stage number
     * @throws IllegalArgumentException if stage is negative
     * @see ExamProfile#getMinStage()
     */
    public void setMinStage(int stage) {
        if (stage < 0)
            throw new IllegalArgumentException("Stage cannot be negative");

        _minStage = stage;
    }
    
    /**
     * Sets the passing score for this Examination as a percentage.
     * @param score the passing score, from 0 to 100
     * @throws IllegalArgumentException if score is negative or &gt; 100
     * @see ExamProfile#getPassScore()
     */
    public void setPassScore(int score) {
        if ((score < 0) || (score > 100))
            throw new IllegalArgumentException("Passing score cannot be < 0% or >100%");
        
        _passScore = score;
    }
    
    /**
     * Sets the number of questions in this Examination.
     * @param size the number of questions
     * @throws IllegalArgumentException if size is negative
     * @see ExamProfile#getSize()
     */
    public void setSize(int size) {
        if (size < 1)
            throw new IllegalArgumentException("Size cannot be zero or negative");
        
        _questions = size;
    }
    
    /**
     * Sets the time required to complete this Examination.
     * @param time the time in minutes
     * @throws IllegalArgumentException if time is zero or negative
     * @see ExamProfile#getTime()
     */
    public void setTime(int time) {
        if (time < 1)
            throw new IllegalArgumentException("Time cannot be zero or negative");

        _time = time;
    }
    
    /**
     * Marks this Examination as available to be taken.
     * @param active TRUE if the Examination is active, otherwise FALSE
     * @see ExamProfile#getActive()
     */
    public void setActive(boolean active) {
        _active = active;
    }
    
    /**
     * Marks this Examination as part of the Flight Academy.
     * @param academy TRUE if the Examination is part of the Flight Academy, otherwise FALSE
     * @see ExamProfile#setAcademy(boolean)
     */
    public void setAcademy(boolean academy) {
    	_flightAcademy = academy;
    }
    
    /**
     * Compares two examinations by comparing their stage and name.
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(Object o2) {
        ExamProfile e2 = (ExamProfile) o2;
        
        // Compare the stages
        int tmpResult = new Integer(_stage).compareTo(new Integer(e2.getStage()));
        return (tmpResult != 0) ? tmpResult : _name.compareTo(e2.getName()); 
    }
    
    /**
     * Returns the Examination name for cache purposes.
     * @return the Examination name
     */
    public Object cacheKey() {
        return getName();
    }
    
    public int hashCode() {
    	return _name.hashCode();
    }

    /**
     * Returns the Examination name.
     */
    public String toString() {
    	return _name;
    }

    /**
     * Returns the CSS class name if displayed in a view table.
     * @return the CSS class name
     */
    public String getRowClassName() {
    	return !_active ? "warn" : (_flightAcademy ? "opt2" : null); 
    }
}