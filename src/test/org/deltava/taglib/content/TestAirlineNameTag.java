// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.content;

import org.deltava.taglib.AbstractTagTestCase;

import org.deltava.util.system.SystemData;

public class TestAirlineNameTag extends AbstractTagTestCase {

	private AirlineNameTag _tag;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_tag = new AirlineNameTag();
		_tag.setPageContext(_ctx);
	}

	@Override
	protected void tearDown() throws Exception {
		_tag.release();
		_tag = null;
		super.tearDown();
	}

	public void testAirlineName() throws Exception {
		assertSkipBody(_tag.doStartTag());
		assertEvalPage(_tag.doEndTag());
		assertEquals(SystemData.get("airline.name"), _jspOut.toString());
	}
}