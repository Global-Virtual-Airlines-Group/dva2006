package org.deltava.taglib.html;

import org.deltava.taglib.AbstractFormTagTestCase;

public class TestFileUploadTag extends AbstractFormTagTestCase {
	
	private FileUploadTag _tag;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
        super.setUp();
        _tag = new FileUploadTag();
        _tag.setPageContext(_ctx);
        _tag.setParent(_formTag);
	}

	@Override
	protected void tearDown() throws Exception {
        _tag.release();
        _tag = null;
		super.tearDown();
	}

	public void testOutput() throws Exception {
        _tag.setClassName("DEFAULTTEXT");
        _tag.setID("InputID");
        _tag.setName("FIELD1");
        _tag.setSize(3);
        _tag.setMax(4);

        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        parseOutput();
        assertEquals("input", getElementName());
        assertAttr("file", "type");
        assertAttr("DEFAULTTEXT", "class");
        assertAttr("InputID", "id");
        assertAttr("FIELD1", "name");
        assertAttr("3", "size");
        assertAttr("4", "maxlength");
	}
}