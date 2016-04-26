package org.deltava.taglib;

import javax.servlet.jsp.*;

import org.deltava.commands.ViewContext;
import org.deltava.taglib.view.*;

public abstract class AbstractScrollTagTestCase extends AbstractTagTestCase {

	protected ViewContext _vctx;
    protected TableTag _tableTag;
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _tableTag = new TableTag();
        _tableTag.setPageContext(_ctx);
        _tableTag.setCmd("table");
        _tableTag.setSize(60);
    }
    
    @Override
	protected void tearDown() throws Exception {
        _tableTag.release();
        _tableTag = null;
        super.tearDown();
    }
    
    protected void initViewContext() {
    	_vctx = new ViewContext(_req, 25);
    	_ctx.setAttribute(ViewContext.VIEW_CONTEXT, _vctx, PageContext.REQUEST_SCOPE);
    }
    
    @SuppressWarnings("static-method")
	protected void testParentValidation(ScrollTag sTag) {
        try {
            assertSkipBody(sTag.doStartTag());
            fail("JspException expected");
        } catch (JspException je) {
            assertEquals(JspTagException.class, je.getClass());
            assertTrue(je.getMessage().endsWith(" Tag must be contained within view:table Tag"));
        }
    }
    
    protected void setStart(int start) {
    	_rootReq.setParameterValue("viewStart", new String[] { String.valueOf(start) } );
    }
    
    protected void setCount(int count) {
    	_rootReq.setParameterValue("viewCount", new String[] { String.valueOf(count) } );
    }
}