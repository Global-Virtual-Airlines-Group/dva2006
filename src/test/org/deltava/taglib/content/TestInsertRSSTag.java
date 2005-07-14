// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.content;

import javax.servlet.jsp.JspException;

import org.deltava.taglib.AbstractTagTestCase;

public class TestInsertRSSTag extends AbstractTagTestCase {

   private InsertRSSTag _tag;
   
   private static final String RSS_START = "<link rel=\"alternate\" type=\"application/rss+xml\" title=\"";
   
   protected void setUp() throws Exception {
      super.setUp();
      _tag = new InsertRSSTag();
      _tag.setPageContext(_ctx);
   }

   protected void tearDown() throws Exception {
      _tag.release();
      _tag = null;
      super.tearDown();
   }

   public void testOutput() throws JspException {
      _tag.setTitle("RSS Feed");
      _tag.setUrl("http://localhost/rss/feed.ws");
      
      assertSkipBody(_tag.doStartTag());
      assertEvalPage(_tag.doEndTag());
      assertEquals(RSS_START + "RSS Feed\" href=\"http://localhost/rss/feed.ws\" />", _jspOut.toString());
   }
   
   public void testURLValidation() {
      try {
         _tag.setUrl("malformed://URLException");
         fail("JspException expected");
      } catch (JspException je) { }
   }
}