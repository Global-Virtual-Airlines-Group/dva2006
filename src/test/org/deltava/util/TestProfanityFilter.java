package org.deltava.util;

import java.io.*;
import java.util.*;

import junit.framework.TestCase;

@Deprecated
public class TestProfanityFilter extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();

		// Load the bad words
		InputStream is = ConfigLoader.getStream("/etc/profanity.txt");
		LineNumberReader lr = new LineNumberReader(new InputStreamReader(is));
		Collection<String> words = new LinkedHashSet<String>();
		while (lr.ready())
			words.add(lr.readLine());

		ProfanityFilter.init(words);
	}

	protected void tearDown() throws Exception {
		ProfanityFilter.clear();
		super.tearDown();
	}

	public void testFlag() {
		assertTrue(ProfanityFilter.flag("You are a filthy mother fucker"));
		assertFalse(ProfanityFilter.flag("A sucker or an idiot is born every minute"));
	}
	
	public void testFilter() {
		assertEquals("You are a filthy ****", ProfanityFilter.filter("You are a filthy mother fucker"));
	}
}