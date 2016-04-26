// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.News;
import org.deltava.beans.Notice;

public class TestNewsAccessControl extends AccessControlTestCase {

   private NewsAccessControl _ac;
   private News _nws;
   
   public static Test suite() {
      return new CoverageDecorator(TestNewsAccessControl.class, new Class[] { AccessControl.class,
            NewsAccessControl.class });
   }
   
   @Override
protected void setUp() throws Exception {
      super.setUp();
      _nws = new News("Subject", "John Smith", "Stuff Happened");
      _ac = new NewsAccessControl(_ctxt, _nws);
   }

   @Override
protected void tearDown() throws Exception {
      _ac = null;
      _nws = null;
      super.tearDown();
   }

   public void testNewsAccess() throws Exception {
      _ac.validate();
      
      assertFalse(_ac.getCanCreateNews());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());
      
      _user.addRole("News");
      _ac.validate();
      
      assertTrue(_ac.getCanCreateNews());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());
      
      _user.addRole("Admin");
      _ac.validate();

      assertTrue(_ac.getCanCreateNews());
      assertTrue(_ac.getCanEdit());
      assertTrue(_ac.getCanDelete());
   }
   
   public void testNOTAMAccess() throws Exception {
      _ac = new NewsAccessControl(_ctxt, new Notice("Subject", "John Smith", "Stuff Happened"));
      _ac.validate();
      
      assertFalse(_ac.getCanCreateNOTAM());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());

      _user.addRole("NOTAM");
      _ac.validate();

      assertTrue(_ac.getCanCreateNOTAM());
      assertTrue(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());
      
      _user.removeRole("NOTAM");
      _user.addRole("HR");
      _ac.validate();

      assertFalse(_ac.getCanCreateNOTAM());
      assertTrue(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());

      _user.addRole("Admin");
      _ac.validate();
      
      assertTrue(_ac.getCanCreateNOTAM());
      assertTrue(_ac.getCanEdit());
      assertTrue(_ac.getCanDelete());
   }
   
   public void testNullNews() throws Exception {
      _ac = new NewsAccessControl(_ctxt, null);
      _user.addRole("HR");
      _ac.validate();
      
      assertFalse(_ac.getCanCreateNews());
      assertFalse(_ac.getCanCreateNOTAM());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());

      _user.addRole("News");
      _ac.validate();
      
      assertTrue(_ac.getCanCreateNews());
      assertFalse(_ac.getCanCreateNOTAM());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());

      _user.removeRole("News");
      _user.addRole("NOTAM");
      _ac.validate();

      assertFalse(_ac.getCanCreateNews());
      assertTrue(_ac.getCanCreateNOTAM());
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanDelete());
   }
   
   public void testContextValidation() {
      doContextValidation(new NewsAccessControl(null, _nws));
   }
}