// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.functions;

import org.deltava.beans.EquipmentType;
import org.deltava.beans.Ranks;

/**
 * A JSP Function Library to define Equipment Program-related functions.
 * @author Luke
 * @version 1.0
 * @since 1.0
 * @see Ranks
 */

public class EquipmentTypeFunctions {

   /**
    * Returns the name of the First Officer's examination for this Equipment Program.
    * @param eq the Equipment Program (can be null)
    * @return the First Officer's examination name
    */
	public static String examFO(EquipmentType eq) {
	   return (eq == null) ? null : eq.getExamName(Ranks.RANK_FO);
	}
	
	 /**
    * Returns the name of the Captain's examination for this Equipment Program.
    * @param eq the Equipment Program (can be null)
    * @return the First Officer's examination name
    */
	public static String examC(EquipmentType eq) {
	   return (eq == null) ? null : eq.getExamName(Ranks.RANK_C);
	}
   
	/**
	 * Returns the number of Flight Legs required for promotion to a particular rank.
	 * @param eq the Equipment Program
	 * @param rank the rank to be promoted to
	 * @return the number of legs required
	 * @throws NullPointerException if eq is null
	 */
	public static int promotionLegs(EquipmentType eq, String rank) {
	   return eq.getPromotionLegs(rank);
	}
	
	/**
	 * Returns the number of Flight Hours required for promotion to a particular rank.
	 * @param eq the Equipment Program
	 * @param rank the rank to be promoted to
	 * @return the number of hours required
	 * @throws NullPointerException if eq is null
	 */
	public static double promotionHours(EquipmentType eq, String rank) {
	   return eq.getPromotionHours(rank);
	}
}