package org.deltava.comparators;

import junit.framework.Test;
import junit.framework.TestCase;

import org.deltava.beans.Rank;
import org.hansel.CoverageDecorator;

public class TestRankComparator extends TestCase {
   
   private RankComparator _rc;
   
   public static Test suite() {
      return new CoverageDecorator(TestRankComparator.class, new Class[] { RankComparator.class } );
  }    

   @Override
protected void setUp() throws Exception {
      super.setUp();
      _rc = new RankComparator();
   }

   @Override
protected void tearDown() throws Exception {
      _rc = null;
      super.tearDown();
   }
   
   public void testIntPromotion() {
      _rc.setRank1(Rank.C, 3);
      _rc.setRank2(Rank.FO, 3);
      assertTrue(_rc.compare() > 0);
   }

   public void testSameStage() {
      _rc.setRank1(Rank.FO, 1);
      _rc.setRank2(Rank.FO, 1);
      assertEquals(0, _rc.compare());
      
      _rc.setRank1(Rank.C, 1);
      _rc.setRank2(Rank.FO, 1);
      assertTrue(_rc.compare() > 0);
      
      _rc.setRank1(Rank.FO, 1);
      _rc.setRank2(Rank.C, 1);
      assertTrue(_rc.compare() < 0);
   }
   
   public void testHigherStage() {
      _rc.setRank1(Rank.FO, 1);
      _rc.setRank2(Rank.FO, 3);
      assertTrue(_rc.compare() < 0);
      
      _rc.setRank1(Rank.C, 1);
      _rc.setRank2(Rank.FO, 2);
      assertTrue(_rc.compare() < 0);

      _rc.setRank1(Rank.C, 1);
      _rc.setRank2(Rank.CP, 5);
      assertTrue(_rc.compare() < 0);

      // James - this was what failed with you
      _rc.setRank1(Rank.SC, 2);
      _rc.setRank2(Rank.FO, 4);
      assertTrue(_rc.compare() < 0);
   }
   
   public void testLowerStage() {
      _rc.setRank1(Rank.FO, 3);
      _rc.setRank2(Rank.FO, 1);
      assertTrue(_rc.compare() > 0);

      _rc.setRank1(Rank.FO, 3);
      _rc.setRank2(Rank.C, 1);
      assertTrue(_rc.compare() > 0);
   }
   
   public void testValidation() {
      _rc.setRank1(Rank.FO, -3);
      _rc.setRank2(Rank.FO, -4);
      _rc.compare(); // just make sure we don't blow up
      
      _rc.setRank1(null, -3);
      _rc.setRank2(Rank.FO, -4);
      _rc.compare(); // just make sure we don't blow up
   }
}