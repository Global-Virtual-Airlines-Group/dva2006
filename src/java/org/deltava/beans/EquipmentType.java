// Copyright 2004, 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.*;

/**
 * A class for storing equipment program information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EquipmentType implements java.io.Serializable, Comparable, ComboAlias, ViewEntry {
	
	// Database constants
	public static final int SECONDARY_RATING = 0;
	public static final int PRIMARY_RATING = 1;
    
    private String _name;
    private int _stage = 1;
    private boolean _active = true;
    private boolean _acarsPromotion;
    
    private String _cpName;
    private String _cpEmail;
    private int _cpID;

    private Collection<String> _ranks = new ArrayList<String>();
    private Collection<String> _primaryRatings = new TreeSet<String>();
    private Collection<String> _secondaryRatings = new TreeSet<String>();
    
    private Map<String, Integer> _promotionCriteria = new HashMap<String, Integer>();
    private Map<String, String> _examNames = new HashMap<String, String>();
    
    /**
     * Create a new EquipmentType object for a given aircraft type
     * @param eqName The name of the equipment type program
     * @throws NullPointerException if the name is null
     * @see EquipmentType#getName() 
     */
    public EquipmentType(String eqName) {
        super();
        _name = eqName.trim();
    }
    
    /**
     * Returns the name of the equipment type program.
     * @return The equipment type name
     */
    public String getName() {
        return _name;
    }
    
    /**
     * Returns the name of this equipment program's Chief Pilot.
     * @return The name of the Chief Pilot
     * @see EquipmentType#setCPEmail(String)
     */
    public String getCPName() {
        return _cpName;
    }
    
    /**
     * Returns the e-mail address of this equipment program's Chief Pilot.
     * @return the e-mail address
     * @see EquipmentType#setCPEmail(String)
     */
    public String getCPEmail() {
        return _cpEmail;
    }
    
    /**
	 * Return the database row ID of this equipment program's chief pilot. <i>This typically will only be called by a DAO</i>
	 * @return The primary key of the entry in the <b>PILOTS</b> table in the database that corresponds
	 * to this Equipment Type's chief pilot.
	 * @see EquipmentType#setCPID(int)
	 */
    public int getCPID() {
        return _cpID;
    }

    /**
     * Returns the equipment type stage.
     * @return The stage number of the equipment type
     * @see EquipmentType#setStage(int)
     */
    public int getStage() {
        return _stage;
    }
    
    /**
     * Return the list of available ranks in this program.
     * @return An <i>unsorted</i> list of available ranks
     * @see EquipmentType#addRank(String)
     * @see EquipmentType#addRanks(String, String)
     */
    public Collection<String> getRanks() {
        return _ranks;
    }
    
    /**
     * Return the list of aircraft types that are considered "primary ratings"
     * @return A sorted list of aircraft types
     * @see EquipmentType#addPrimaryRating(String)
     * @see EquipmentType#getSecondaryRatings()
     */
    public Collection<String> getPrimaryRatings() {
        return _primaryRatings;
    }

    /**
     * Return the list of aircraft types that are considered "secondary ratings"
     * @return A sorted list of aircraft types
     * @see EquipmentType#addSecondaryRating(String)
     * @see EquipmentType#getPrimaryRatings()
     */
    public Collection<String> getSecondaryRatings() {
        return _secondaryRatings;
    }
    
    /**
     * Return the name of the examination required for promotion into a rank
     * @param rank The rank to be promoted <i>into</i>. Use Ranks.ENTRY for an entrance exam.
     * @return The name of the examination, null if none
     * @see Ranks
     * @see EquipmentType#setExamName(String, String)
     */
    public String getExamName(String rank) {
        return _examNames.get(rank);
    }
    
    /**
     * Return the number of hours required for promotion
     * @param rank The <i>current</i> rank of the pilot, use constants when you can
     * @return The number of hours required for a promotion <i>out of</i> the specified rank, returns 0 if not set
     * @throws NullPointerException if rank is null 
     * @see Ranks
     * @see EquipmentType#setPromotionHours(String, int)
     * @see EquipmentType#getPromotionLegs(String)
     */
    public int getPromotionHours(String rank) {
        Integer hours = _promotionCriteria.get(rank + "_HOURS");
        return (hours == null) ? 0 : hours.intValue();
    }

    /**
     * Return the number of flight legs required for promotion
     * @param rank The <i>current</i> rank of the pilot, use constants when you can
     * @return The number of legs required for a promotion <i>out of</i> the specified rank, returns 0 if not set
     * @throws NullPointerException if rank is null 
     * @see Ranks
     * @see EquipmentType#setPromotionLegs(String, int)
     * @see EquipmentType#getPromotionHours(String)
     */
    public int getPromotionLegs(String rank) {
        Integer legs = _promotionCriteria.get(rank + "_LEGS");
        return (legs == null) ? 0 : legs.intValue();
    }
    
    /**
     * Does this equipment type have a Second Officer rank?
     * @return TRUE if this program has a Second Officer rank, otherwise FALSE
     * @see EquipmentType#getRanks()
     * @see EquipmentType#addRank(String)
     */
    public boolean hasSO() {
        return _ranks.contains(Ranks.RANK_SO);
    }
    
    /**
     * Returns wether flights counting towards promotion must be logged using ACARS.
     * @return TRUE if promotion legs must be logged using ACARS, otherwise FALSE
     * @see EquipmentType#setACARSPromotionLegs(boolean)
     */
    public boolean getACARSPromotionLegs() {
    	return _acarsPromotion;
    }
    
    /**
     * Returns wether this equipment program is active.
     * @return TRUE if the program is active, otherwise FALSE
     * @see EquipmentType#getActive()
     */
    public boolean getActive() {
        return _active;
    }
    
    /**
     * Add an available rank to this equipment type.
     * @param rank The rank name
     * @throws NullPointerException if rank is null
     * @see EquipmentType#getRanks()
     * @see EquipmentType#addRanks(String, String)
     */
    public void addRank(String rank) {
        if (rank == null)
            throw new NullPointerException("Rank cannot be null");
        
        _ranks.add(rank);
    }
    
    /**
     * Adds a number of ranks to this equipment type.
     * @param ranks A token-delimited string of ranks
     * @param delim A token delimieter for the ranks parameter
     * @throws NullPointerException if the list string is null
     * @see EquipmentType#addRank(String)
     * @see EquipmentType#getRanks()
     */
    public void addRanks(String ranks, String delim) {
        StringTokenizer rankTokens = new StringTokenizer(ranks, delim);
        while (rankTokens.hasMoreTokens()) {
            addRank(rankTokens.nextToken());
        }
    }
    
    /**
     * Updates the available ranks for this Equipment Program.
     * @param ranks a Collection of rank names
     */
    public void setRanks(Collection<String> ranks) {
    	_ranks.clear();
    	_ranks.addAll(ranks);
    }

    /**
     * Adds a primary rating to the list and removes it from the secondary rating list.
     * @param rating The aircraft type to add
     * @throws NullPointerException if the rating is null
     * @see EquipmentType#getPrimaryRatings()
     */
    public void addPrimaryRating(String rating) {
        if (_secondaryRatings.contains(rating))
            _secondaryRatings.remove(rating);
        
        _primaryRatings.add(rating);
    }
    
    /**
     * Adds a secondary rating to the list and removes it from the primary rating list.
     * @param rating The aircraft type to add
     * @throws NullPointerException if the rating is null
     * @see EquipmentType#getSecondaryRatings()
     */
    public void addSecondaryRating(String rating) {
        if (_primaryRatings.contains(rating))
            _primaryRatings.remove(rating);
        
        _secondaryRatings.add(rating);
    }
    
    /**
     * Marks this equipment program as active.
     * @param active TRUE if the program is active, otherwise FALSE
     * @see EquipmentType#getActive()
     */
    public void setActive(boolean active) {
        _active = active;
    }
    
    /**
     * Updates wether flights counting towards promotion must be logged using ACARS.
     * @param useACARS TRUE if flights must be logged using ACARS, otherwise FALSE
     * @see EquipmentType#setACARSPromotionLegs(boolean)
     */
    public void setACARSPromotionLegs(boolean useACARS) {
    	_acarsPromotion = useACARS;
    }
    
    /**
     * Loads primary/secondary ratings.
     * @param pr a Collection of primary ratings
     * @param sr a Collection of secondary ratings
     */
    public void setRatings(Collection<String> pr, Collection<String> sr) {
    	_primaryRatings.clear();
    	if (pr != null)
    	   _primaryRatings.addAll(pr);
    	
    	_secondaryRatings.clear();
    	if (sr != null)
    	   _secondaryRatings.addAll(sr);
    }
    
    /**
     * Set the name of this equipment program's Chief Pilot.
     * @param cpName The name of the Chief Pilot
     * @see EquipmentType#getCPName()
     */
    public void setCPName(String cpName) {
        _cpName = cpName;
    }
    
    /**
     * Updates the e-mail address of this program's Chief Pilot.
     * @param email The e-mail address
     * @see EquipmentType#getCPEmail()
     */
    public void setCPEmail(String email) {
        _cpEmail = email;
    }

	/**
	 * Update the database row ID of this program's Chief Pilot. <i>This typically will only be called by a DAO</i>
	 * @param id The primary key of the entry in the <b>PILOTS</b> table in the database that corresponds
	 * to this Equipment Program's chief pilot.
	 * @throws IllegalArgumentException if the database ID is negative
	 * @see EquipmentType#getCPID()
	 */
    public void setCPID(int id) {
	    if (id < 1)
	        throw new IllegalArgumentException("Database ID cannot be negative");

	    _cpID = id;
    }
    
    /**
     * Set the examination required for promotion into a particular rank
     * @param rank The rank to be promoted <i>into</i>. Use Ranks.ENTRY for an entrance exam.
     * @param examName The name of the examination.
     * @see Ranks
     * @see EquipmentType#getExamName(String)
     */
    public void setExamName(String rank, String examName) {
        _examNames.put(rank, examName);
    }
    
    /**
     * Sets the stage for this equipment type program
     * @param stage The stage number for this program
     * @throws IllegalArgumentException if stage is negative
     * @see EquipmentType#getStage()
     */
    public void setStage(int stage) {
        if (stage < 1)
            throw new IllegalArgumentException("Stage cannot be negative");
        
        _stage = stage;
    }
    
    /**
     * Set the number of hours required for promotion
     * @param rank The <i>current</i> rank of the pilot, use constants when you can
     * @param hours The number of hours required for promotion
     * @throws IllegalArgumentException if hours is negative
     * @throws NullPointerException if rank is null
     * @see Ranks
     * @see EquipmentType#getPromotionHours(String)
     * @see EquipmentType#setPromotionLegs(String, int)
     */
    public void setPromotionHours(String rank, int hours) {
        if (hours < 0)
            throw new IllegalArgumentException("Hours cannot be zero");
        
        _promotionCriteria.put(rank + "_HOURS", new Integer(hours));
    }

    /**
     * Set the number of legs required for promotion.
     * @param rank The <i>current</i> rank of the pilot, use constants when you can
     * @param legs The number of legs required for promotion
     * @throws IllegalArgumentException if legs is negative
     * @throws NullPointerException if rank is null
     * @see Ranks
     * @see EquipmentType#getPromotionLegs(String)
     * @see EquipmentType#setPromotionHours(String, int)
     */
    public void setPromotionLegs(String rank, int legs) {
        if (legs < 0)
            throw new IllegalArgumentException("Legs cannot be zero");
        
        _promotionCriteria.put(rank + "_LEGS", new Integer(legs));
    }
    
    /**
     * Compares programs by comparing stage values, then the name.
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     * @see String#compareTo(String)
     */
    public int compareTo(Object o2) {
        EquipmentType et2 = (EquipmentType) o2; 
        int tmp = new Integer(getStage()).compareTo(new Integer(et2.getStage()));
        return (tmp == 0) ? getName().compareTo(et2.getName()) : tmp;
    }
    
    /**
     * Determine equality by comparing the program names.
     * @see String#equals(Object)
     */
    public boolean equals(Object o2) {
       return (o2 != null) ? _name.equals(o2.toString()) : false;
    }

    /**
     * Returns the name's hashcode.
     */
    public int hashCode() {
    	return _name.hashCode();
    }
    
    /**
     * When converting to a string, just return the name.
     * @see Object#toString()
     */
    public String toString() {
       return getName();
    }

    public String getComboAlias() {
        return getName();
    }

    public String getComboName() {
        return getName();
    }
    
    public String getRowClassName() {
    	return _active ? null : "opt2";
    }
}