// Copyright 2005, 2007, 2008, 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import org.deltava.beans.Rank;

/**
 * A Comparator to compare pilot rank/equipment program changes. This isn't a true comparator since it
 * doesn't implement {@link java.util.Comparator}, but it returns similar values.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class RankComparator {

   private RankStage _rs1;
   private RankStage _rs2;
   
   protected class RankStage implements Comparable<RankStage> {
      
      private final Rank _rank;
      private final int _stage;
      
      RankStage(Rank rank, int stage) {
         super();
         _rank = rank;
         _stage = stage;
      }
      
      // Compare stages, and only ranks if stages are equal
      @Override
      public int compareTo(RankStage rs2) {
         int tmpResult = Integer.valueOf(_stage).compareTo(Integer.valueOf(rs2._stage));
         return (tmpResult == 0) ? _rank.compareTo(rs2._rank) : tmpResult;
      }
   }
   
   /**
    * Sets the first rank/stage pair.
    * @param rank the rank
    * @param stage the stage
    */
   public void setRank1(Rank rank, int stage) {
      _rs1 = new RankStage(rank, stage);
   }
   
   /**
    * Sets the second rank/stage pair.
    * @param rank the rank
    * @param stage the stage
    */
   public void setRank2(Rank rank, int stage) {
      _rs2 = new RankStage(rank, stage);
   }
   
   /**
    * Compares the two rank/stage pairs.
    * @return -1 if the first is less than the second, 1 if the first is greater than the second, otherwise 0
    * @see Comparable#compareTo(Object)
    */
   public int compare() {
      return _rs1.compareTo(_rs2);
   }
}