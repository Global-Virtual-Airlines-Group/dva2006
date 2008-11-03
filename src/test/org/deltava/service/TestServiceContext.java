// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

import javax.servlet.http.*;

import junit.framework.Test;
import junit.framework.TestCase;
import org.hansel.CoverageDecorator;

import com.kizna.servletunit.*;

import org.deltava.beans.Pilot;

public class TestServiceContext extends TestCase {
   
   private ServiceContext _ctx;
   
   private HttpServletRequest _req;
   private HttpServletResponse _rsp;
   
   public static Test suite() {
      return new CoverageDecorator(TestServiceContext.class, new Class[] { ServiceContext.class } );
  }

   protected void setUp() throws Exception {
      super.setUp();
      _req = new HttpServletRequestSimulator();
      _rsp = new HttpServletResponseSimulator();
      _ctx = new ServiceContext(_req, _rsp);
   }

   protected void tearDown() throws Exception {
      _ctx = null;
      super.tearDown();
   }

   public void testProperties() {
      assertEquals(_req, _ctx.getRequest());
      assertEquals(_rsp, _ctx.getResponse());
      
      assertNull(_ctx.getUser());
      assertFalse(_ctx.isAuthenticated());
      assertNotNull(_ctx.getRoles());
      assertTrue(_ctx.isUserInRole("Anonymous"));
      assertTrue(_ctx.isUserInRole("*"));
      assertFalse(_ctx.isUserInRole("Pilot"));
      
      Pilot p = new Pilot("Test", "Pilot");
      _ctx.setUser(p);
      
      assertNotNull(_ctx.getUser());
      assertTrue(_ctx.isAuthenticated());
      assertNotNull(_ctx.getRoles());
      assertEquals(p.getRoles(), _ctx.getRoles());
      
      assertTrue(_ctx.isUserInRole("Pilot"));
      assertTrue(_ctx.isUserInRole("*"));

   }
}
