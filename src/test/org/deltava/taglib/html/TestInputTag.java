package org.deltava.taglib.html;

import org.deltava.taglib.AbstractFormTagTestCase;

public class TestInputTag extends AbstractFormTagTestCase {
    
    private InputTag _tag;

    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _tag = new InputTag();
        _tag.setPageContext(_ctx);
        _tag.setParent(_formTag);
    }

    @Override
	protected void tearDown() throws Exception {
        _tag.release();
        _tag = null;
        super.tearDown();
    }

    public void testProperties() throws Exception {
        _tag.setClassName("DEFAULTTEXT");
        _tag.setID("InputID");
        _tag.setName("FIELD1");
        _tag.setSize(3);
        _tag.setMax(4);
        
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        parseOutput();
        assertEquals("input", getElementName());
        assertEquals(6, getAttrCount());
        
        assertAttr("InputID", "id");
        assertAttr("3", "size");
        assertAttr("4", "maxlength");
        assertAttr("text", "type");
        assertAttr("FIELD1", "name");
        assertAttr("DEFAULTTEXT", "class");
    }
    
    public void testBooleanProperties() throws Exception {
        _tag.setName("FIELD2");
        _tag.setDisabled(true);
        _tag.setReadOnly(true);
        
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        parseOutput();
        assertEquals("input", getElementName());
        assertEquals(4, getAttrCount());
        
        assertAttr("text", "type");
        assertAttr("FIELD2", "name");
        assertAttr("readonly", "readonly");
        assertAttr("disabled", "disabled");
        
        _jspOut.clearBuffer();
        _tag.setName("FIELD2");
        _tag.setDisabled(false);
        _tag.setReadOnly(false);
        
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        parseOutput();
        assertEquals("input", getElementName());
        assertEquals(2, getAttrCount());

        assertAttr("text", "type");
        assertAttr("FIELD2", "name");
    }
    
    public void testNameException() {
        _tag.setClassName("CLASSNAME");
        testNameValidation(_tag);
    }
    
    public void testParentFormException() {
        _tag.setName("FIELD4");
        _tag.setParent(null);
        testParentValidation(_tag);
    }
    
    public void testNumericValidation() throws Exception {
        _tag.setName("FIELD3");
        _tag.setSize(-3);
        _tag.setIdx("0");
        
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        parseOutput();
        assertEquals("input", getElementName());
        assertEquals(2, getAttrCount());

        assertAttr("text", "type");
        assertAttr("FIELD3", "name");

        _jspOut.clearBuffer();
        _tag.setName("FIELD3");

        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        parseOutput();
        assertEquals("input", getElementName());
        assertEquals(2, getAttrCount());

        assertAttr("text", "type");
        assertAttr("FIELD3", "name");
    }
}