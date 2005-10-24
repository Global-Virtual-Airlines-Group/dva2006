// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.stats;

import java.io.Serializable;

/**
 * A bean to store Water Cooler statistics.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CoolerStatsEntry implements Serializable, Comparable {

   private Comparable _label;
   private int _posts;
   private int _distinct;
   
   /**
    * Creates a new Water Cooler statistics bean.
    * @param label the label
    * @param posts the number of posts
    */
   public CoolerStatsEntry(Comparable label, int posts, int distinct) {
      super();
      _label = label;
      _posts = posts;
      _distinct = distinct;
   }
   
   /**
    * Returns the statistics label.
    * @return the label
    */
   public Object getLabel() {
      return _label;
   }
   
   public int getDistinct() {
      return _distinct;
   }
   
   /**
    * Returns the number of posts.
    * @return the number of posts
    */
   public int getPosts() {
      return _posts;
   }
   
   /**
    * Compares two statistics entries by comparing their labels.
    * @see Comparable#compareTo(Object)
    */
   public int compareTo(Object o2) {
      CoolerStatsEntry e2 = (CoolerStatsEntry) o2;
      return _label.compareTo(e2._label);
   }
}