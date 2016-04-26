// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.security.command;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.gallery.*;

public class TestGalleryAccessControl extends AccessControlTestCase {

   private GalleryAccessControl _ac;
   private Image _img;
   
   public static Test suite() {
      return new CoverageDecorator(TestGalleryAccessControl.class, new Class[] { AccessControl.class,
            GalleryAccessControl.class });
   }
   
   @Override
protected void setUp() throws Exception {
      super.setUp();
      _img = new Image("Image", "Description");
      _img.setID(321);
      _ac = new GalleryAccessControl(_ctxt, _img);
   }

   @Override
protected void tearDown() throws Exception {
      _ac = null;
      _img = null;
      super.tearDown();
   }
   
   public void testAccess() throws Exception {
      _user.addRole("Pilot");
      _ac.validate();
      
      assertFalse(_ac.getCanEdit());
      assertTrue(_ac.getCanLike());
      assertTrue(_ac.getCanCreate());
      assertFalse(_ac.getCanDelete());
      
      _img.addLike(_user.getID());
      assertTrue(_img.hasLiked(_user));
      _ac.validate();
      
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanLike());
      assertTrue(_ac.getCanCreate());
      assertFalse(_ac.getCanDelete());
      
      _user.removeRole("Pilot");
      _ac.validate();
      
      assertTrue(_ac.getCanEdit());
      assertFalse(_ac.getCanLike());
      assertFalse(_ac.getCanCreate());
      assertFalse(_ac.getCanDelete());

      _user.addRole("Admin");
      _ac.validate();

      assertTrue(_ac.getCanEdit());
      assertFalse(_ac.getCanLike());
      assertTrue(_ac.getCanCreate());
      assertTrue(_ac.getCanDelete());
   }

   public void testAnonymousAccess() throws Exception {
      _ctxt.logoff();
      _ac.validate();
      
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanLike());
      assertFalse(_ac.getCanCreate());
      assertFalse(_ac.getCanDelete());
   }
   
   public void testNullImage() throws Exception {
      _ac = new GalleryAccessControl(_ctxt, null);
      _ac.validate();
      
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanLike());
      assertFalse(_ac.getCanCreate());
      assertFalse(_ac.getCanDelete());

      _user.addRole("Pilot");
      _ac.validate();
      
      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanLike());
      assertTrue(_ac.getCanCreate());
      assertFalse(_ac.getCanDelete());

      _ctxt.logoff();
      _ac.validate();

      assertFalse(_ac.getCanEdit());
      assertFalse(_ac.getCanLike());
      assertFalse(_ac.getCanCreate());
      assertFalse(_ac.getCanDelete());
   }
   
   public void testContextValidation() {
      doContextValidation(new GalleryAccessControl(null, _img));
   }
}
