package org.deltava.taglib.content;

import org.deltava.taglib.AbstractTagTestCase;

import org.deltava.util.system.SystemData;

public class TestGetSystemDataTag extends AbstractTagTestCase {

	private GetSystemDataTag _tag;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_tag = new GetSystemDataTag();
		_tag.setPageContext(_ctx);
	}

	@Override
	protected void tearDown() throws Exception {
		_tag = null;
		super.tearDown();
	}

	public void testTag() throws Exception {
		_tag.setVar("attrName");
		_tag.setName("path.css");
		assertNotNull(SystemData.get("path.css"));
		
		assertEvalPage(_tag.doEndTag());
		
		assertNotNull(_req.getAttribute("attrName"));
		assertEquals(SystemData.get("path.css"), _req.getAttribute("attrName"));
	}
}