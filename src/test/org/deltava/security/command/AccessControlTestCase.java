package org.deltava.security.command;

import java.util.*;

import javax.servlet.http.*;

import com.kizna.servletunit.*;
import junit.framework.TestCase;

import org.deltava.beans.Person;
import org.deltava.commands.CommandSecurityException;
import org.deltava.security.SecurityContext;

public abstract class AccessControlTestCase extends TestCase {

	protected HttpServletRequest _request;
	protected AccessControlContext _ctxt;
	protected AccessControlUser _user;
	
	protected class AccessControlContext implements SecurityContext {
	   
	   private final List<String> ANONYMOUS_ROLES = Arrays.asList(new String[] {"Anonymous"});
	   
	   private Person _usr;
	   private HttpServletRequest _req;
	   
	   public AccessControlContext(Person usr, HttpServletRequest req) {
	      super();
	      _usr = usr;
	      _req = req;
	   }

	   public boolean isAuthenticated() {
	      return (_usr != null);
	   }

	   public Person getUser() {
	      return _usr;
	   }

	   public Collection<String> getRoles() {
	      return isAuthenticated() ? _usr.getRoles() : ANONYMOUS_ROLES;
	   }

	   public boolean isUserInRole(String roleName) {
	      if (isAuthenticated())
	      	return _usr.isInRole(roleName);
	      
	      return ("*".equals(roleName) || ANONYMOUS_ROLES.contains(roleName));
	   }

	   public HttpServletRequest getRequest() {
	      return _req;
	   }
	   
	   public void logoff() {
	      _usr = null;
	   }
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		_request = new HttpServletRequestSimulator();
		_user = new AccessControlUser("Test", "User");
		_user.setID(123);
		_ctxt = new AccessControlContext(_user, _request);
	}
	
	protected void tearDown() throws Exception {
		_ctxt = null;
		_user = null;
		_request = null;
		super.tearDown();
	}
	
	protected void doContextValidation(AccessControl ac) {
	   try {
	      ac.validate();
	      fail("IllegalStateException expected");
	   } catch (CommandSecurityException cse) {
	      fail("IllegalStateException expected");
	   } catch (IllegalStateException ise) { }
	}
}