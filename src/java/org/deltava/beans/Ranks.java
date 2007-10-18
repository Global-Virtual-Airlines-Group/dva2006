// Copyright 2004, 2005, 2007 Global Virtual Airlines Group. All Righs Reserved.
package org.deltava.beans;

/**
 * A holder interface for rank name constants. Other ranks may be defined, but these are used internally.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface Ranks {

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
     * Name of Second Officer rank. <i>Used by Aviation Francais Virtuel</i>
     */
    public static final String RANK_SO = "Second Officer";
}