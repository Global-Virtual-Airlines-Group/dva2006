// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.comparators;

import java.util.*;

/**
 * A Comparator to compare pilot rank/equipment program changes. This isn't a true comparator since it
 * doesn't implement {@link java.util.Comparator}, but it retunrs
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class RankComparator {

   private List<String> _ranks;
   
   private RankStage _rs1;
   private RankStage _rs2;
   
   public RankComparator(Collection<String> ranks) {
      super();
      _ranks = new ArrayList<String>(ranks);
   }
   
   protected class RankStage implements Comparable<RankStage> {
      
      private String _rank;
      private int _stage;
      
      RankStage(String rank, int stage) {
         super();
         _rank = rank;
         _stage = stage;
      }
      
      public int compareTo(RankStage rs2) {
         // Compare stages, and only ranks if stages are equal
         int tmpResult = Integer.valueOf(_stage).compareTo(Integer.valueOf(rs2._stage));
         return (tmpResult == 0) ? compareRanks(_rank, rs2._rank) : tmpResult;
      }
   }
   
   /**
    * Compares two ranks.
    * @param r1 the first rank
    * @param r2 the second rank
    * @return -1 if r1 < r2, 1 if r2 > r1, otherwise 0
    * @see Comparable#compareTo(Object)
    */
   public int compareRanks(String r1, String r2) {
      if (_ranks.isEmpty())
         throw new IllegalStateException("Ranks not Populated");
      
      int ofs1 = _ranks.indexOf(r1);
      int ofs2 = _ranks.indexOf(r2);
      return new Integer(ofs1).compareTo(new Integer(ofs2));
   }
   
   /**
    * Sets the first rank/stage pair.
    * @param rank the rank
    * @param stage the stage
    */
   public void setRank1(String rank, int stage) {
      _rs1 = new RankStage(rank, stage);
   }
   
   /**
    * Sets the second rank/stage pair.
    * @param rank the rank
    * @param stage the stage
    */
   public void setRank2(String rank, int stage) {
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