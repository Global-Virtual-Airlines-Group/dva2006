// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.comparators;

import java.util.*;

import junit.framework.Test;
import junit.framework.TestCase;

import org.hansel.CoverageDecorator;

public class TestRankComparator extends TestCase {
   
   private static final List<String> RANKS = Arrays.asList(new String[] {"Trainee", "First Officer", "Captain", "Senior Captain",
         "Assistant Chief Pilot", "Chief Pilot"});
   
   private RankComparator _rc;
   
   public static Test suite() {
      return new CoverageDecorator(TestRankComparator.class, new Class[] { RankComparator.class } );
  }    

   protected void setUp() throws Exception {
      super.setUp();
      _rc = new RankComparator(RANKS);
   }

   protected void tearDown() throws Exception {
      _rc = null;
      super.tearDown();
   }
   
   public void testIntPromotion() {
      _rc.setRank1("Captain", 3);
      _rc.setRank2("First Officer", 3);
      assertTrue(_rc.compare() > 0);
   }

   public void testSameStage() {
      _rc.setRank1("First Officer", 1);
      _rc.setRank2("First Officer", 1);
      assertEquals(0, _rc.compare());
      
      _rc.setRank1("Captain", 1);
      _rc.setRank2("First Officer", 1);
      assertTrue(_rc.compare() > 0);
      
      _rc.setRank1("First Officer", 1);
      _rc.setRank2("Captain", 1);
      assertTrue(_rc.compare() < 0);
   }
   
   public void testHigherStage() {
      _rc.setRank1("First Officer", 1);
      _rc.setRank2("First Officer", 3);
      assertTrue(_rc.compare() < 0);
      
      _rc.setRank1("Captain", 1);
      _rc.setRank2("First Officer", 2);
      assertTrue(_rc.compare() < 0);

      _rc.setRank1("Captain", 1);
      _rc.setRank2("Chief Pilot", 5);
      assertTrue(_rc.compare() < 0);

      // James - this was what failed with you
      _rc.setRank1("Senior Captain", 2);
      _rc.setRank2("First Officer", 4);
      assertTrue(_rc.compare() < 0);
   }
   
   public void testLowerStage() {
      _rc.setRank1("First Officer", 3);
      _rc.setRank2("First Officer", 1);
      assertTrue(_rc.compare() > 0);

      _rc.setRank1("First Officer", 3);
      _rc.setRank2("Captain", 1);
      assertTrue(_rc.compare() > 0);
      
      _rc.setRank1("Captain", 2);
      _rc.setRank2("Trainee", 1);
      assertTrue(_rc.compare() > 0);
   }
   
   public void testValidation() {
      _rc.setRank1("First Officer", -3);
      _rc.setRank2("First Officer", -4);
      _rc.compare(); // just make sure we don't blow up
      
      _rc.setRank1(null, -3);
      _rc.setRank2("First Officer", -4);
      _rc.compare(); // just make sure we don't blow up
      
      RankComparator rc2 = new RankComparator(new ArrayList<String>());
      rc2.setRank1("First Officer", 3);
      rc2.setRank2("First Officer", 3);
      try {
         rc2.compare();
         fail("IllegalStateException expected");
      } catch (IllegalStateException ise) {
    	// empty
      }
   }
}