package org.deltava.taglib.content;

import org.deltava.taglib.AbstractTagTestCase;

public class TestInsertJSTag extends AbstractTagTestCase {

   private InsertJSTag _tag;

   private static final String JS_START = "<script language=\"JavaScript\" type=\"text/javascript\" src=\"";
   private static final String GOOGLE_JS = "http://maps.google.com/maps?file=api&v=1&key=abcdefg";

   @Override
protected void setUp() throws Exception {
      super.setUp();
      _tag = new InsertJSTag();
      _tag.setPageContext(_ctx);
   }

   @Override
protected void tearDown() throws Exception {
      _tag.release();
      _tag = null;
      super.tearDown();
   }

   public void testOutput() throws Exception {
      _tag.setName("jslib");
      assertSkipBody(_tag.doStartTag());
      assertEvalPage(_tag.doEndTag());
      assertEquals(JS_START + "/jslib/jslib.js\"></script>", _jspOut.toString());

      _jspOut.clearBuffer();
      _tag.setName("javascriptlib2");
      assertSkipBody(_tag.doStartTag());
      assertEvalPage(_tag.doEndTag());
      assertEquals(JS_START + "/jslib/javascriptlib2.js\"></script>", _jspOut.toString());
   }

   public void testRemoteFile() throws Exception {
      _tag.setName(GOOGLE_JS);
      assertSkipBody(_tag.doStartTag());
      assertEvalPage(_tag.doEndTag());
      assertEquals(JS_START + GOOGLE_JS + "\"></script>", _jspOut.toString());
   }
}