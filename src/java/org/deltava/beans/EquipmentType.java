// Copyright 2004, 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.*;

import org.deltava.beans.system.AirlineInformation;
import org.deltava.util.cache.Cacheable;

/**
 * A class for storing equipment program information.
 * @author Luke
 * @version 2.7
 * @since 1.0
 */

public class EquipmentType implements Cacheable, Comparable<EquipmentType>, ComboAlias, ViewEntry {
	
	// Database constants
	public static final int SECONDARY_RATING = 0;
	public static final int PRIMARY_RATING = 1;
	public static final int EXAM_FO = 0;
	public static final int EXAM_CAPT = 1;
	
    private String _name;
    private int _stage = 1;
    private boolean _active = true;
    private boolean _acarsPromotion;
    
    private String _cpName;
    private String _cpEmail;
    private int _cpID;
    
    private int _promotionLegs;
    private int _promotionHours;
    private int _promotionMinLength;

    private final Collection<String> _ranks = new LinkedHashSet<String>();
    private final Collection<String> _primaryRatings = new TreeSet<String>();
    private final Collection<String> _secondaryRatings = new TreeSet<String>();
    
    private final Map<String, Collection<String>> _examNames = new HashMap<String, Collection<String>>();
    
    private AirlineInformation _owner;
    private final Collection<AirlineInformation> _airlines = new TreeSet<AirlineInformation>();
    
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
     * Return the list of aircraft types that are considered "primary ratings".
     * @return a sorted list of aircraft types
     * @see EquipmentType#addPrimaryRating(String)
     * @see EquipmentType#getSecondaryRatings()
     * @see EquipmentType#getRatings()
     */
    public Collection<String> getPrimaryRatings() {
        return _primaryRatings;
    }

    /**
     * Return the list of aircraft types that are considered "secondary ratings".
     * @return a sorted list of aircraft types
     * @see EquipmentType#addSecondaryRating(String)
     * @see EquipmentType#getPrimaryRatings()
     * @see EquipmentType#getRatings()
     */
    public Collection<String> getSecondaryRatings() {
        return _secondaryRatings;
    }
    
    /**
     * Returns all primary and secondary ratings.
     * @return a sorted list of aircraft types
     * @see EquipmentType#getPrimaryRatings()
     * @see EquipmentType#getSecondaryRatings()
     * @see EquipmentType#addPrimaryRating(String)
     * @see EquipmentType#addSecondaryRating(String)
     */
    public Collection<String> getRatings() {
    	final Collection<String> results = new TreeSet<String>(_primaryRatings);
    	results.addAll(_secondaryRatings);
    	return results;
    }
    
    /**
     * Return the name of the examination required for promotion into a rank
     * @param rank The rank to be promoted <i>into</i>. Use Ranks.ENTRY for an entrance exam.
     * @return The names of the examinations
     * @see EquipmentType#getExamNames()
     * @see EquipmentType#setExamNames(String, Collection)
     */
    public Collection<String> getExamNames(String rank) {
    	Collection<String> names = _examNames.get(rank);
    	return (names == null) ? new HashSet<String>() : names;
    }
    
    /**
     * Returns all exams associated with this equipment program.
     * @return a Collection of exam names
     * @see EquipmentType#getExamNames(String)
     * @see EquipmentType#setExamNames(String, Collection)
     */
    public Collection<String> getExamNames() {
    	Collection<String> names = new LinkedHashSet<String>();
    	for (Iterator<Collection<String>> i = _examNames.values().iterator(); i.hasNext(); )
    		names.addAll(i.next());
    	
    	return names;
    }
    
    /**
     * Return the number of hours required for promotion to Captain.
     * @return The number of hours required for a promotion <i>out of</i> the specified rank, returns 0 if not set
     * @see EquipmentType#setPromotionHours(int)
     * @see EquipmentType#getPromotionLegs()
     */
    public int getPromotionHours() {
    	return _promotionHours;
    }

    /**
     * Return the number of flight legs required for promotion to Captain.
     * @return The number of legs required for a promotion <i>out of</i> the specified rank, returns 0 if not set
     * @see EquipmentType#setPromotionLegs(int)
     * @see EquipmentType#getPromotionHours()
     */
    public int getPromotionLegs() {
    	return _promotionLegs;
    }
    
    /**
     * Returns the minimum flight leg distance requires for promotion to Captain.
     * @return the minimum leg distance in miles
     * @see EquipmentType#setPromotionMinLength(int)
     */
    public int getPromotionMinLength() {
    	return _promotionMinLength;
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
     * Returns the Airline that owns this equipment program profile.
     * @return an AirlineInformation bean
     * @see EquipmentType#setOwner(AirlineInformation)
     */
    public AirlineInformation getOwner() {
    	return _owner;
    }
    
    /**
     * Returns the Airlines whose pilots can get ratings via this equipment program.
     * @return a Collection of AirlineInformation beans
     * @see EquipmentType#addAirline(AirlineInformation)
     * @see EquipmentType#setAirlines(Collection)
     */
    public Collection<AirlineInformation> getAirlines() {
    	return _airlines;
    }
    
    /**
     * Add an available rank to this equipment type.
     * @param rank The rank
     * @see EquipmentType#getRanks()
     * @see EquipmentType#addRanks(String, String)
     */
    public void addRank(String rank) {
        if (rank != null)
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
        while (rankTokens.hasMoreTokens())
            addRank(rankTokens.nextToken());
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
     * Adds an examination required for promotion into a particular rank.
     * @param rank The rank to be promoted <i>into</i>
     * @param examName The name of the examination
     * @see Ranks
     * @see EquipmentType#setExamNames(String, Collection)
     * @see EquipmentType#getExamNames(String)
     */
    public void addExam(String rank, String examName) {
    	Collection<String> eNames = _examNames.get(rank);
    	if (eNames == null) {
    		eNames = new LinkedHashSet<String>();
    		_examNames.put(rank, eNames);
    	}
    	
    	eNames.add(examName);
    }
    
    /**
     * Set the examinations required for promotion into a particular rank.
     * @param rank The rank to be promoted <i>into</i>
     * @param examNames The name of the examinations
     * @see Ranks
     * @see EquipmentType#addExam(String, String)
     * @see EquipmentType#getExamNames(String)
     */
    public void setExamNames(String rank, Collection<String> examNames) {
    	Collection<String> eNames = _examNames.get(rank);
    	if (eNames == null) {
    		eNames = new LinkedHashSet<String>();
    		_examNames.put(rank, eNames);
    	} else
    		eNames.clear();
    	
    	if (examNames != null)
    		eNames.addAll(examNames);
    }
    
    /**
     * Updates the Airline that owns this equipment program.
     * @param ai an AirlineInformation bean
     * @see EquipmentType#getOwner()
     */
    public void setOwner(AirlineInformation ai) {
    	_owner = ai;
    	_airlines.add(ai);
    }
    
    /**
     * Adds an Airline to the list of applications whose Pilots can get ratings in this equipment program.
     * @param ai an AirlineInformation bean
     * @see EquipmentType#getAirlines()
     * @see EquipmentType#setAirlines(Collection)
     */
    public void addAirline(AirlineInformation ai) {
    	_airlines.add(ai);
    }
    
    /**
     * Updates the list of applications whose Pilots can get ratings in this equipment program.
     * @param airlines a Collection of AirlineInformation beans
     * @see EquipmentType#getAirlines()
     * @see EquipmentType#addAirline(AirlineInformation)
     */
    public void setAirlines(Collection<AirlineInformation> airlines) {
    	_airlines.clear();
    	if (airlines != null)
    		_airlines.addAll(airlines);
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
     * Set the number of hours required for promotion to Cpatain.
     * @param hours The number of hours required for promotion
     * @see EquipmentType#getPromotionHours()
     * @see EquipmentType#setPromotionLegs(int)
     */
    public void setPromotionHours(int hours) {
    	_promotionHours = Math.max(0, hours);
    }

    /**
     * Set the number of legs required for promotion to Captain.
     * @param legs The number of legs required for promotion
     * @see EquipmentType#getPromotionLegs()
     * @see EquipmentType#setPromotionHours(int)
     */
    public void setPromotionLegs(int legs) {
    	_promotionLegs = Math.max(0, legs);
    }
    
    /**
     * Sets the minimum length of a leg for promotion to Captain.
     * @param distance the distance in miles
     * @see EquipmentType#getPromotionMinLength()
     */
    public void setPromotionMinLength(int distance) {
    	_promotionMinLength = Math.max(0, distance);
    }
    
    /**
     * Compares programs by comparing stage values, then the name.
     */
    public int compareTo(EquipmentType et2) {
        int tmp = Integer.valueOf(_stage).compareTo(Integer.valueOf(et2._stage));
        return (tmp == 0) ? _name.compareTo(et2._name) : tmp;
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
    
    public Object cacheKey() {
    	return _owner.getDB() + "!!" + _name;
    }
    
    public String getRowClassName() {
    	return _active ? null : "opt2";
    }
}