package org.deltava.taglib.format;

import org.deltava.taglib.AbstractTagTestCase;

public class TestFileSizeFormatTag extends AbstractTagTestCase {
	
	private FileSizeFormatTag _tag;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_tag = new FileSizeFormatTag();
	}

	@Override
	protected void tearDown() throws Exception {
		_tag = null;
		super.tearDown();
	}

	public void testBytes() throws Exception {
		_tag.setPageContext(_ctx);
        _tag.setFmt("#0.0");
        
        _tag.setValue(Long.valueOf(2));
        _tag.setShowBytes(false);
        assertEvalPage(_tag.doEndTag());
        assertEquals("2", _jspOut.toString());
        _jspOut.clearBuffer();
        
        _tag.setValue(Long.valueOf(299));
        assertEvalPage(_tag.doEndTag());
        assertEquals("299B", _jspOut.toString());
        _jspOut.clearBuffer();
        
        _tag.setValue(Long.valueOf(1299));
        assertEvalPage(_tag.doEndTag());
        assertEquals("1,299B", _jspOut.toString());
        _jspOut.clearBuffer();
        
        _tag.setValue(Long.valueOf(8299));
        assertEvalPage(_tag.doEndTag());
        assertEquals("8,299B", _jspOut.toString());
        _jspOut.clearBuffer();
        
        _tag.setValue(Long.valueOf(34999));
        assertEvalPage(_tag.doEndTag());
        assertEquals("35KB", _jspOut.toString());
        _jspOut.clearBuffer();
	}
}
