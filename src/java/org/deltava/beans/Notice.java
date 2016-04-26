// Copyright 2005, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

/**
 * A class for storing Notice to Airmen (NOTAM) entries.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class Notice extends News implements ViewEntry {
   
   private boolean _active;
   private boolean _isHTML;

   /**
    * Creates a new NOTAM. The NOTAM will automatically be marked active.
    * @param sbj the NOTAM subject
    * @param aN the Author Name
    * @param body the body of the NOTAM
    * @see News#News(String, String, String)
    */
   public Notice(String sbj, String aN, String body) {
      super(sbj, aN, body);
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
    * Returns if this NOTAM is raw HTML.
    * @return TRUE if the Notiice is raw HTML, otherwise FALSE
    * @see Notice#getIsHTML()
    */
   public boolean getIsHTML() {
	   return _isHTML;
   }
   
   /**
    * Updates if this NOTAM is in effect.
    * @param active TRUE if in effect, otherwise FALSE
    * @see Notice#getActive()
    */
   public void setActive(boolean active) {
      _active = active;
   }
   
   /**
    * Updates if this Notice is raw HTML.
    * @param html TRUE if raw HTML text, otherwise FALSE
    * @see Notice#getIsHTML()
    */
   public void setIsHTML(boolean html) {
	   _isHTML = html;
   }
   
   /**
    * Returns the CSS class name if displayed in a view.
    */
   @Override
   public String getRowClassName() {
      return _active ? null : "opt1";
   }
}