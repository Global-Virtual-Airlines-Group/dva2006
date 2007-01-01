package org.deltava.comparators;

import org.deltava.beans.Pilot;
import org.deltava.beans.Ranks;

/**
 * A comparator for sorting Pilot objects.
 * @author Luke
 * @version 1.0
 * @since 1.0
 * @see PersonComparator
 */
@SuppressWarnings("hiding")
public class PilotComparator extends PersonComparator<Pilot> {

	
	public static final int PILOTCODE = 4;
    public static final int EQTYPE = 5;
    public static final int RANK = 6;
    public static final int LEGS = 7;
    public static final int HOURS = 8;
    public static final int STATUS = 9;
    
    public static final String[] TYPES = { "First Name", "Last Name", "Login Date", "Creation Date", "Pilot Code",
            "Equipment Type", "Rank", "Flight Legs", "Flight Hours", "Status"};
    
    private static final String[] RANKS = {"Trainee", Ranks.RANK_SO, Ranks.RANK_FO, Ranks.RANK_C, "Senior Captain",
            "Assistant Chief Pilot", Ranks.RANK_CP};
    
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
    
    private int getRankValue(String rank) {
        for (int x = 0; x < RANKS.length; x++) {
            if (RANKS[x].equals(rank))
                return x;
        }
        
        return - 1;
    }
    
    private int comparePilotCodes(int pc1, int pc2) {
        // Handle the fact that unassinged pilots appear later in the list
        if ((pc1 == 0) && (pc2 > 0)) {
            return 1;
        } else if ((pc2 == 0) && (pc1 > 0)) {
            return -1;
        }
        
        // Do a regular comparison
        return new Integer(pc1).compareTo(new Integer(pc2));
    }

    /**
     * Compares two pilot objects by the designated criteria.
     * @throws ClassCastException if either object is not a Pilot
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    protected int compareImpl(Pilot p1, Pilot p2) {
        
        // If we are using a comparison method that is implemented in the superclass, call it
        if (_comparisonType < PilotComparator.PILOTCODE)
            return super.compareImpl(p1, p2);

        switch (_comparisonType) {
        		case PILOTCODE :
        		    return comparePilotCodes(p1.getPilotNumber(), p2.getPilotNumber());
        		    
        		case EQTYPE :
        		    return p1.getEquipmentType().compareTo(p2.getEquipmentType());
        		    
        		case RANK :
        		    return new Integer(getRankValue(p1.getRank())).compareTo(new Integer(getRankValue(p2.getRank())));
        		    
        		case LEGS :
        		    return new Integer(p1.getLegs()).compareTo(new Integer(p2.getLegs()));
        		    
        		case STATUS :
        		    return new Integer(p1.getStatus()).compareTo(new Integer(p2.getStatus()));
        		    
        		default :   
        		case HOURS :
        		    return new Double(p1.getHours()).compareTo(new Double(p2.getHours()));
        }
    }
}