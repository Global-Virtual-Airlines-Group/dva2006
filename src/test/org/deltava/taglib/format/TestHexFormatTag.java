package org.deltava.taglib.format;

import org.deltava.taglib.AbstractTagTestCase;

public class TestHexFormatTag extends AbstractTagTestCase {

	private HexFormatTag _tag;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_tag = new HexFormatTag();
        _tag.setPageContext(_ctx);
	}

	@Override
	protected void tearDown() throws Exception {
		_tag.release();
		_tag = null;
		super.tearDown();
	}

	public void testTag() throws Exception {
		_tag.setValue(32);
        assertEvalPage(_tag.doEndTag());
        assertEquals("20", _jspOut.toString());
	}
	
	public void testTagSpan() throws Exception {
		_tag.setValue(35);
		_tag.setClassName("hexTag");
		
		assertEvalPage(_tag.doEndTag());
		assertEquals("<span class=\"hexTag\">23</span>", _jspOut.toString());
	}
}