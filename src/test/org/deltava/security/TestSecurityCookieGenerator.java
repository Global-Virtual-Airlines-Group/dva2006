package org.deltava.security;

import java.time.Instant;

import javax.servlet.http.Cookie;

import junit.framework.Test;
import junit.framework.TestCase;
import org.hansel.CoverageDecorator;

import org.deltava.commands.CommandContext;
import org.deltava.crypt.*;

@SuppressWarnings("static-method")
public class TestSecurityCookieGenerator extends TestCase {

	private static final String _desKey = "SuperSecret 24-byte 3DESKey";
	private static final String _aesKey = "SuperSecret 24-byte AESKey";
	
    public static Test suite() {
        return new CoverageDecorator(TestSecurityCookieGenerator.class, new Class[] { SecurityCookieGenerator.class } );
    }

	public void testGeneration() {
	    SecurityCookieGenerator.init(new DESEncryptor(_desKey));
	    
	    Instant now = Instant.now();
	    
	    SecurityCookieData d1 = new SecurityCookieData("cn=Luke,ou=dva,o=sce");
	    d1.setRemoteAddr("127.0.0.1");
	    d1.setExpiryDate(now);
	    
	    Cookie c = new Cookie(CommandContext.AUTH_COOKIE_NAME, SecurityCookieGenerator.getCookieData(d1));
		assertEquals(CommandContext.AUTH_COOKIE_NAME, c.getName());
		assertNotNull(c.getValue());
		assertEquals(0, c.getVersion());
		assertEquals(-1, c.getMaxAge());
		assertNull(c.getPath());
		
		SecurityCookieData d2 = SecurityCookieGenerator.readCookie(c.getValue());
		assertEquals(d1.getUserID(), d2.getUserID());
		assertEquals(d1.getRemoteAddr(), d2.getRemoteAddr());
	}
	
	public void testAES() {
		SecurityCookieGenerator.init(new AESEncryptor(_aesKey));

		Instant now = Instant.now();
	    
	    SecurityCookieData d1 = new SecurityCookieData("cn=Luke,ou=dva,o=sce");
	    d1.setRemoteAddr("127.0.0.1");
	    d1.setExpiryDate(now);
	    
	    Cookie c = new Cookie(CommandContext.AUTH_COOKIE_NAME, SecurityCookieGenerator.getCookieData(d1));
		assertEquals(CommandContext.AUTH_COOKIE_NAME, c.getName());
		assertNotNull(c.getValue());

		SecurityCookieData d2 = SecurityCookieGenerator.readCookie(c.getValue());
		assertEquals(d1.getUserID(), d2.getUserID());
		assertEquals(d1.getRemoteAddr(), d2.getRemoteAddr());
	}
	
	public void testInvalidCookie() {
	    SecurityCookieGenerator.init(new DESEncryptor(_desKey));

	    Instant now = Instant.now();

	    SecurityCookieData d1 = new SecurityCookieData("cn=Luke,ou=dva,o=sce");
	    d1.setRemoteAddr("127.0.0.1");
	    d1.setExpiryDate(now);

	    fail("Not Implemented");
		// Pass it into a cookie with a different 
	}
}