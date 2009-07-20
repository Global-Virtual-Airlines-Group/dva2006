// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.Applicant;

public class TestApplicantAccessControl extends AccessControlTestCase {
   
   private ApplicantAccessControl _ac;
   private Applicant _a;
   
   public static Test suite() {
      return new CoverageDecorator(TestApplicantAccessControl.class, 
            new Class[] { AccessControl.class, ApplicantAccessControl.class } );
  }

   protected void setUp() throws Exception {
      super.setUp();
      _a = new Applicant("Test", "User");
      _ac = new ApplicantAccessControl(_ctxt, _a);
   }

   protected void tearDown() throws Exception {
      _a = null;
      _ac = null;
      super.tearDown();
   }
   
   public void testAccess() throws Exception {
      _ac.validate();
      
      assertFalse(_ac.getCanRead());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanApprove());
      assertFalse(_ac.getCanReject());
      
      _a.setID(_user.getID());
      _ac.validate();
      assertTrue(_ac.getCanRead());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanApprove());
      assertFalse(_ac.getCanReject());
   }
   
   public void testHRAccess() throws Exception {
      _a.setStatus(Applicant.APPROVED);
      _user.addRole("HR");
      _ac.validate();
      assertTrue(_ac.getCanRead());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanApprove());
      assertFalse(_ac.getCanReject());

      _a.setStatus(Applicant.PENDING);
      _ac.validate();
      assertTrue(_ac.getCanRead());
      assertTrue(_ac.getCanEdit());
      assertTrue(_ac.getCanApprove());
      assertTrue(_ac.getCanReject());
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
      doContextValidation(new ApplicantAccessControl(null, _a));
   }
}