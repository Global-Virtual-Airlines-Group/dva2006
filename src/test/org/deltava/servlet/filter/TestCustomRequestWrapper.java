package org.deltava.servlet.filter;

import javax.servlet.http.*;

import junit.framework.Test;
import junit.framework.TestCase;
import com.kizna.servletunit.*;
import org.hansel.CoverageDecorator;

import org.deltava.beans.Pilot;
import org.deltava.commands.HTTPContext;

public class TestCustomRequestWrapper extends TestCase {
	
	private HttpServletRequestSimulatorHelper _req;
	private CustomRequestWrapper _wreq;
	
	// Quick dirty helper class to ensure that getSession(false) works properly
	private class HttpServletRequestSimulatorHelper extends HttpServletRequestSimulator {
		
		private HttpSession _s;
		
		public HttpServletRequestSimulatorHelper() {
			super();
		}
		
		public HttpSession getSession(boolean force) {
			if (force)
				_s = super.getSession(true);
			
			return _s;
		}
	}
	
	public static Test suite() {
		return new CoverageDecorator(TestCustomRequestWrapper.class, new Class[] { CustomRequestWrapper.class } );
   }

	protected void setUp() throws Exception {
		super.setUp();
		_req = new HttpServletRequestSimulatorHelper();
		_wreq = new CustomRequestWrapper(_req);
	}

	protected void tearDown() throws Exception {
		_wreq = null;
		_req = null;
		super.tearDown();
	}

	public void testAnonymous() {
		assertEquals(HttpServletRequest.FORM_AUTH, _wreq.getAuthType());
		assertNull(_wreq.getRemoteUser());
		assertNull(_wreq.getUserPrincipal());
		assertFalse(_wreq.isUserInRole("AnyRole"));
		assertTrue(_wreq.isUserInRole("Anonymous"));
		assertTrue(_wreq.isUserInRole("*"));
	}
	
	public void testAuthenticated() {
		HttpSession s = _req.getSession(true);
		Pilot p = new Pilot("John", "Smith");
		p.addRole("HR");
		s.setAttribute(HTTPContext.USER_ATTR_NAME, p);
		assertEquals(p, _wreq.getUserPrincipal());
		assertEquals("John Smith", _wreq.getRemoteUser());
		assertTrue(_wreq.isUserInRole("*"));
		assertTrue(_wreq.isUserInRole("HR"));
		assertFalse(_wreq.isUserInRole("Some other role"));
		
		// Test after logoff
		s.removeAttribute(HTTPContext.USER_ATTR_NAME);
		assertNull(_wreq.getRemoteUser());
		assertNull(_wreq.getUserPrincipal());
		assertFalse(_wreq.isUserInRole("AnyRole"));
		assertTrue(_wreq.isUserInRole("Anonymous"));
		assertTrue(_wreq.isUserInRole("*"));
	}
}