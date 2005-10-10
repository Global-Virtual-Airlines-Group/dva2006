package org.deltava.beans;

/**
 * A holder interface for rank name constants. Other ranks may be defined, but these are used internally.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface Ranks {

    /**
     * A placeholder for rank denoting "entry into a particular program".
     */
    public static final String ENTRY = "$entry";
    
    /**
     * Name of Chief Pilot rank.
     */
    public static final String RANK_CP = "Chief Pilot";
    
    /**
     * Name of Assistant Chief Pilot rank.
     */
    public static final String RANK_ACP = "Assistant Chief Pilot";

    /** 
     * Name of Captain rank.
     */
    public static final String RANK_C = "Captain";
    
    /**
     * Name of First Officer's rank. 
     */
    public static final String RANK_FO = "First Officer";
    
    /**
     * Name of Senior Captain rank.
     */
    public static final String RANK_SC = "Senior Captain";
    
    /**
     * Name of Second Officer rank. <i>Used by Aviation Fran�ais Virtuel</i>
     */
    public static final String RANK_SO = "Second Officer";
}