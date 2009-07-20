// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import java.io.Serializable;

/**
 * A bean to store Water Cooler statistics.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CoolerStatsEntry<T> implements Serializable, Comparable<CoolerStatsEntry<T>> {

   private Comparable<T> _label;
   private int _posts;
   private int _distinct;
   
   /**
    * Creates a new Water Cooler statistics bean.
    * @param label the label
    * @param posts the number of posts
    */
   public CoolerStatsEntry(Comparable<T> label, int posts, int distinct) {
      super();
      _label = label;
      _posts = posts;
      _distinct = distinct;
   }
   
   /**
    * Returns the statistics label.
    * @return the label
    */
   public Comparable<T> getLabel() {
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
   @SuppressWarnings("unchecked")
   public int compareTo(CoolerStatsEntry<T> e2) {
      return _label.compareTo((T) e2._label);
   }
}