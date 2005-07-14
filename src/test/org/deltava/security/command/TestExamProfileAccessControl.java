// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.commands.CommandSecurityException;

public class TestExamProfileAccessControl extends AccessControlTestCase {

   private ExamProfileAccessControl _ac;

   public static Test suite() {
      return new CoverageDecorator(TestExamProfileAccessControl.class, new Class[] { AccessControl.class,
            ExamProfileAccessControl.class });
   }

   protected void setUp() throws Exception {
      super.setUp();
      _ac = new ExamProfileAccessControl(_ctxt);
   }

   protected void tearDown() throws Exception {
      _ac = null;
      super.tearDown();
   }

   public void testAccess() throws Exception {
      try {
         _ac.validate();
         fail("CommandSecurityException expected");
      } catch (CommandSecurityException cse) { }

      _user.addRole("Examination");
      _ac.validate();
      
      assertTrue(_ac.getCanRead());
      assertFalse(_ac.getCanEdit());
      
      _user.addRole("HR");
      _ac.validate();
      
      assertTrue(_ac.getCanRead());
      assertTrue(_ac.getCanEdit());
   }
   
   public void testAnonymousAccess() throws Exception {
      _ctxt.logoff();
      try {
         _ac.validate();
         fail("CommandSecurityException expected");
      } catch (CommandSecurityException cse) { }
   }
   
   public void testContextValidation() {
      doContextValidation(new ExamProfileAccessControl(null));
   }
}