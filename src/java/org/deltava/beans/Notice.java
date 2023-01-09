// Copyright 2005, 2016, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * A class for storing Notice to Airmen (NOTAM) entries.
 * @author Luke
 * @version 10.4
 * @since 1.0
 */

public class Notice extends News implements ViewEntry {
   
   private boolean _active;

   /**
    * Creates a new NOTAM. The NOTAM will automatically be marked active.
    * @param sbj the NOTAM subject
    * @param body the body of the NOTAM
    */
   public Notice(String sbj, String body) {
      super(sbj, body);
      _active = true;
   }
   
   /**
    * Returns if this NOTAM is in effect.
    * @return TRUE if in effect, otherwise FALSE
    * @see Notice#setActive(boolean)
    */
   public boolean getActive() {
      return _active;
   }
   
   /**
    * Updates if this NOTAM is in effect.
    * @param active TRUE if in effect, otherwise FALSE
    * @see Notice#getActive()
    */
   public void setActive(boolean active) {
      _active = active;
   }
   
   @Override
   public String getRowClassName() {
      return _active ? null : "opt1";
   }
}