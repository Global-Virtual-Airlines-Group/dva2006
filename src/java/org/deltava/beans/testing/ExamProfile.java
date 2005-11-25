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
    
    private boolean _requiresCheckride;
    
    /**
     * Creates a new Examination profile.
     * @param name the name of the examination
     * @throws NullPointerException if name is null
     */
    public ExamProfile(String name) {
        super();
        _name = name.trim();
    }
    
    /**
     * Returns the Examination Name.
     * @return the exam name
     */
    public String getName() {
        return _name;
    }
    
    // TODO JavaDoc
    public String getEquipmentType() {
        return _eqType;
    }

    public int getStage() {
        return _stage;
    }
    
    public int getMinStage() {
        return _minStage;
    }
    
    public int getSize() {
        return _questions;
    }
    
    public int getPassScore() {
        return _passScore;
    }
    
    public int getTime() {
        return _time;
    }
    
    public boolean getActive() {
        return _active;
    }
    
    public boolean getNeedsCheckRide() {
        return _requiresCheckride;
    }
    
    public void setStage(int stage) {
        if (stage < 1)
            throw new IllegalArgumentException("Stage cannot be negative");
        
        _stage = stage;
    }
    
    public void setEquipmentType(String eqType) {
        _eqType = eqType;
    }
    
    public void setMinStage(int stage) {
        if (stage < 0)
            throw new IllegalArgumentException("Stage cannot be negative");

        _minStage = stage;
    }
    
    public void setPassScore(int score) {
        if ((score < 0) || (score > 100))
            throw new IllegalArgumentException("Passing score cannot be < 0% or >100%");
        
        _passScore = score;
    }
    
    public void setSize(int size) {
        if (size < 1)
            throw new IllegalArgumentException("Size cannot be zero or negative");
        
        _questions = size;
    }
    
    public void setTime(int time) {
        if (time < 1)
            throw new IllegalArgumentException("Time cannot be zero or negative");

        _time = time;
    }
    
    public void setActive(boolean active) {
        _active = active;
    }
    
    public void setNeedsCheckRide(boolean needsRide) {
        _requiresCheckride = needsRide;
    }
    
    public int compareTo(Object o2) {
        ExamProfile e2 = (ExamProfile) o2;
        
        // Compare the stages
        int tmpResult = new Integer(_stage).compareTo(new Integer(e2.getStage()));
        if (tmpResult != 0)
            return tmpResult;
        
        // Then compare the minimum stage required
        tmpResult = new Integer(_minStage).compareTo(new Integer(e2.getMinStage()));
        if (tmpResult != 0)
            return tmpResult;
        
        // Compare the name
        return _name.compareTo(e2.getName());
    }
    
    public boolean equals(ExamProfile e2) {
        return _name.equals(e2.getName());
    }
    
    public Object cacheKey() {
        return getName();
    }
    
    public int hashCode() {
        return _name.hashCode();
    }
    
    public String toString() {
    	return _name;
    }
    
    public String getRowClassName() {
    	return _active ? null : "warn"; 
    }
}