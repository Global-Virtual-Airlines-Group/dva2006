package org.deltava.taglib.html;

import org.deltava.taglib.AbstractFormTagTestCase;

public class TestTextboxTag extends AbstractFormTagTestCase {

    private TextboxTag _tag;
    
    protected void setUp() throws Exception {
        super.setUp();
        _tag = new TextboxTag();
        _tag.setPageContext(_ctx);
        _tag.setParent(_formTag);
    }

    protected void tearDown() throws Exception {
        _tag.release();
        _tag = null;
        super.tearDown();
    }

    public void testProperties() throws Exception {
        _tag.setName("BODY");
        _tag.setIdx("1");
        _tag.setHeight(5);
        _tag.setWidth("100");
        
        assertEvalBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        parseOutput();
        assertEquals("textarea", getElementName());
        assertEquals(4, getAttrCount());
        
        assertAttr("100", "cols");
        assertAttr("5", "rows");
        assertAttr("1", "tabindex");
        assertAttr("BODY", "name");
    }
    
    public void testStyleWidth() throws Exception {
        _tag.setName("BODY");
        _tag.setIdx("*");
        _tag.setHeight(5);
        _tag.setWidth("90%");

        assertEvalBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        parseOutput();
        assertEquals("textarea", getElementName());
        assertEquals(4, getAttrCount());

        assertAttr("5", "rows");
        assertAttr("1", "tabindex");
        assertAttr("BODY", "name");
        assertAttr("width:90%;", "style");
    }
    
    public void testBooleanProperties() throws Exception {
        _tag.setName("BODY");
        _tag.setDisabled(true);
        _tag.setReadOnly(true);
        
        assertEvalBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        parseOutput();
        assertEquals("textarea", getElementName());
        assertEquals(3, getAttrCount());
        
        assertAttr("BODY", "name");
        assertAttr("readonly", "readonly");
        assertAttr("disabled", "disabled");
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
        _tag.setName("BODY");
        _tag.setHeight(-1);
        _tag.setIdx(null);
        
        assertEvalBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        parseOutput();
        assertEquals("textarea", getElementName());
        assertEquals(1, getAttrCount());

        assertAttr("BODY", "name");
    }
}