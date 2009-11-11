// Copyright 2005, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.functions;

import java.util.Collection;

import org.deltava.beans.*;

/**
 * A JSP Function Library to define Equipment Program-related functions.
 * @author Luke
 * @version 2.7
 * @since 1.0
 * @see Ranks
 */

public class EquipmentTypeFunctions {

   /**
    * Returns the name of the First Officer's examination for this Equipment Program.
    * @param eq the Equipment Program (can be null)
    * @return the First Officer's examination name
    */
	public static Collection<String> examFO(EquipmentType eq) {
	   return (eq == null) ? null : eq.getExamNames(Ranks.RANK_FO);
	}
	
	 /**
    * Returns the name of the Captain's examination for this Equipment Program.
    * @param eq the Equipment Program (can be null)
    * @return the First Officer's examination name
    */
	public static Collection<String> examC(EquipmentType eq) {
	   return (eq == null) ? null : eq.getExamNames(Ranks.RANK_C);
	}
}