// Copyright 2004, 2005, 2008, 2009, 2010, 2012, 2106, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import java.time.Instant;

import org.deltava.beans.*;

/**
 * A comparator for sorting Pilot objects.
 * @author Luke
 * @version 8.0
 * @since 1.0
 */

@SuppressWarnings("hiding")
public class PilotComparator extends PersonComparator<Pilot> {

	public static final int PILOTCODE = 4;
    public static final int EQTYPE = 5;
    public static final int RANK = 6;
    public static final int LEGS = 7;
    public static final int HOURS = 8;
    public static final int STATUS = 9;
    public static final int LASTFLIGHT = 10;
    
    public static final String[] TYPES = { "First Name", "Last Name", "Login Date", "Creation Date", "Pilot Code",
            "Equipment Type", "Rank", "Flight Legs", "Flight Hours", "Status", "Last Flight"};
    
    /**
     * Creates a new comparator with a specified comparison type code.
     * @param comparisonType the comparison type code
     */
    public PilotComparator(int comparisonType) {
        super(TYPES);
        setComparisonType(comparisonType);
    }
    
    /**
     * Creates a new comparator with a specified comparison type name.
     * @param comparisonType the comparison type name
     */
    public PilotComparator(String comparisonType) {
        super(TYPES);
        setComparisonType(comparisonType);
    }
    
    private static int comparePilotCodes(int pc1, int pc2) {
        // Handle the fact that unassinged pilots appear later in the list
        if ((pc1 == 0) && (pc2 > 0))
            return 1;
        else if ((pc2 == 0) && (pc1 > 0))
            return -1;
        
        return Integer.compare(pc1, pc2);
    }

    @Override
	protected int compareImpl(Pilot p1, Pilot p2) {
        
        // If we are using a comparison method that is implemented in the superclass, call it
        if (_comparisonType < PilotComparator.PILOTCODE)
            return super.compareImpl(p1, p2);

        int tmpResult = 0;
        switch (_comparisonType) {
        		case PILOTCODE :
        		    return comparePilotCodes(p1.getPilotNumber(), p2.getPilotNumber());
        		    
        		case EQTYPE :
        		    tmpResult = p1.getEquipmentType().compareTo(p2.getEquipmentType());
        		    break;
        		    
        		case RANK :
        		    tmpResult = p1.getRank().compareTo(p2.getRank());
        		    break;
        		    
        		case LEGS :
        		    tmpResult = Integer.compare(p1.getLegs(), p2.getLegs());
        		    break;
        		    
        		case STATUS :
        		    tmpResult = Integer.compare(p1.getStatus(), p2.getStatus());
        		    break;
        		    
        		case LASTFLIGHT:
        			Instant lf1 = p1.getLastFlight();
        			Instant lf2 = p2.getLastFlight();
        			if (lf1 == null)
        				tmpResult = (lf2 == null) ? 0 : -1;
        			else if (lf2 == null)
        				tmpResult = 1;
        			else
        				tmpResult = lf1.compareTo(lf2);
        			
        			break;
        		    
        		default :   
        		case HOURS :
        		    return new Double(p1.getHours()).compareTo(new Double(p2.getHours()));
        }
        
        return (tmpResult == 0) ? Integer.compare(p1.getID(), p2.getID()) : tmpResult;
    }
}