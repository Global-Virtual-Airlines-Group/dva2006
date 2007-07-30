// Copyright 2005, 2006, 2007 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.beans.cooler;

/**
 * An interface to store Water Cooler emoticon names.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface Emoticons {

   /**
    * Emoticon names.
    */
   public static final String[] ICON_NAMES = {"smile", "wink", "cool", "frown", "eek", "mad", "redface", "confused",
         "rolleyes", "biggrin", "razz", "plotting", "judge", "slitwrist", "scared", "evilgrin", "rofl", "nuts", "blahblah", "rawk"};
   
   /**
    * Emoticon codes.
    */
   public static final String[] ICON_CODES = {":)", ";)", null, ":(", ":O", null, null, null, null, ":D", ":p", null, null, 
         null, null, ">)", null, null, null, null};
}