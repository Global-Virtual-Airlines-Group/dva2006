// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.Date;

/**
 * A marker interface for common ACRS log entry functions.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface ACARSLogEntry extends Comparable<ACARSLogEntry> {
   
	/**
	 * Returns the database ID of the Pilot.
	 * @return the Pilot's database ID
	 */
   public int getPilotID();
   
   /**
    * Returns the date/time of this entry.
    * @return the entry date/time
    */
   public Date getStartTime();
}