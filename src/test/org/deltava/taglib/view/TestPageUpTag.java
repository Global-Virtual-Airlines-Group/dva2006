package org.deltava.taglib.view;

public class TestPageUpTag extends AbstractScrollTagTestCase {

    private PageUpTag _tag;
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _tag = new PageUpTag();
        _tag.setPageContext(_ctx);
        _tag.setParent(_tableTag);
    }

    @Override
	protected void tearDown() throws Exception {
        _tag = null;
        super.tearDown();
    }

    public void testAttributes() throws Exception {
        setStart(20);
        setCount(10);
        initViewContext();
        _tag.setText("Previous Page");
        
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("<a href=\"table.do?viewCount=10&amp;viewStart=10\">Previous Page</a>", _jspOut.toString());
    }
    
    public void testDefaults() throws Exception {
        assertSkipBody(_tag.doStartTag());
        assertEquals("", _jspOut.toString());
    }
    
    public void testStartOfView() throws Exception {
        setStart(0);
        assertSkipBody(_tag.doStartTag());
        assertEquals("", _jspOut.toString());
    }
    
    public void testParentTagException() {
        _tag.setParent(null);
        testParentValidation(_tag);
    }
}