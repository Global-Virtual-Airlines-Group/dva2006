// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import junit.framework.Test;

import org.deltava.beans.testing.Examination;
import org.hansel.CoverageDecorator;

public class TestExamAccessControl extends AccessControlTestCase {
   
   private ExamAccessControl _ac;
   private Examination _exam;
   
   public static Test suite() {
      return new CoverageDecorator(TestExamAccessControl.class, 
            new Class[] { AccessControl.class, ExamAccessControl.class } );
  }

   protected void setUp() throws Exception {
      super.setUp();
      _exam = new Examination("Dummy");
      _ac = new ExamAccessControl(_ctxt, _exam, null);
   }

   protected void tearDown() throws Exception {
      _ac = null;
      _exam = null;
      super.tearDown();
   }

   public void testExaminationAccess() throws Exception {
      _user.addRole("Examination");
      assertFalse(_user.getID() == _exam.getPilotID());
      assertEquals(org.deltava.beans.testing.Test.NEW, _exam.getStatus());
      _ac.validate();
      
      assertTrue(_ac.getCanRead());
      assertFalse(_ac.getCanSubmit());
      assertFalse(_ac.getCanScore());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());
      
      _exam.setStatus(org.deltava.beans.testing.Test.SUBMITTED);
      _ac.validate();

      assertTrue(_ac.getCanRead());
      assertFalse(_ac.getCanSubmit());
      assertTrue(_ac.getCanScore());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());
      
      _exam.setStatus(org.deltava.beans.testing.Test.SCORED);
      _ac.validate();
      
      assertTrue(_ac.getCanRead());
      assertFalse(_ac.getCanSubmit());
      assertFalse(_ac.getCanScore());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());
   }
   
   public void testHRAccess() throws Exception {
      _user.addRole("HR");
      assertFalse(_user.getID() == _exam.getPilotID());
      assertEquals(org.deltava.beans.testing.Test.NEW, _exam.getStatus());
      _ac.validate();
      
      assertTrue(_ac.getCanRead());
      assertFalse(_ac.getCanSubmit());
      assertFalse(_ac.getCanScore());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());
      
      _exam.setStatus(org.deltava.beans.testing.Test.SUBMITTED);
      _ac.validate();

      assertTrue(_ac.getCanRead());
      assertFalse(_ac.getCanSubmit());
      assertTrue(_ac.getCanScore());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());
      
      _exam.setStatus(org.deltava.beans.testing.Test.SCORED);
      _ac.validate();
      
      assertTrue(_ac.getCanRead());
      assertFalse(_ac.getCanSubmit());
      assertTrue(_ac.getCanScore());
      assertTrue(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());
   }
   
   public void testScoreLimits() throws Exception {
      _user.addRole("HR");
      _exam.setPilotID(_user.getID());
      _exam.setStatus(org.deltava.beans.testing.Test.SUBMITTED);
      _ac.validate();
      
      assertTrue(_ac.getCanRead());
      assertFalse(_ac.getCanSubmit());
      assertFalse(_ac.getCanScore());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());

      _exam.setStatus(org.deltava.beans.testing.Test.SCORED);
      _ac.validate();
      
      assertTrue(_ac.getCanRead());
      assertFalse(_ac.getCanSubmit());
      assertFalse(_ac.getCanScore());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());
   }
   
   public void testUserAccess() throws Exception {
      assertFalse(_user.getID() == _exam.getPilotID());
      try {
         _ac.validate();
         fail("AccessControlException expected");
      } catch (AccessControlException cse) {
    	  // empty
      }
      
      assertEquals(org.deltava.beans.testing.Test.NEW, _exam.getStatus());
      _exam.setPilotID(_user.getID());
      _ac.validate();
      
      assertTrue(_ac.getCanRead());
      assertTrue(_ac.getCanSubmit());
      assertFalse(_ac.getCanScore());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());
      
      _exam.setStatus(org.deltava.beans.testing.Test.SUBMITTED);
      _ac.validate();
      
      assertTrue(_ac.getCanRead());
      assertFalse(_ac.getCanSubmit());
      assertFalse(_ac.getCanScore());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());
      
      _exam.setStatus(org.deltava.beans.testing.Test.SCORED);
      _ac.validate();

      assertTrue(_ac.getCanRead());
      assertFalse(_ac.getCanSubmit());
      assertFalse(_ac.getCanScore());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());
   }
   
   public void testAnonymousAccess() {
      _ctxt.logoff();
      try {
         _ac.validate();
         fail("AccessControlException expected");
      } catch (AccessControlException cse) {
    	// empty
      }
   }
   
   public void testContextValidation() {
      doContextValidation(new ExamAccessControl(null, _exam, null));
   }
}