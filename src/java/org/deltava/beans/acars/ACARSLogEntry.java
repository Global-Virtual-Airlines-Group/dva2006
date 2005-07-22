// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.Date;

/**
 * A marker interface for common ACRS log entry functions.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface ACARSLogEntry extends Comparable {
   
   public int getPilotID();
   public Date getStartTime();
}