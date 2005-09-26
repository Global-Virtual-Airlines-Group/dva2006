// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.taglib.content;

import java.net.URL;
import java.net.MalformedURLException;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;

import org.deltava.taglib.ContentHelper;

/**
 * A JSP Tag to insert a link to an RSS data feed.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InsertRSSTag extends InsertContentTag {

   private String _title;
   private URL _url;
   
   public void setTitle(String title) {
      _title = title;
   }
   
   public void setUrl(String url) throws JspException {
      try {
         _url = new URL(url);
      } catch (MalformedURLException mue) {
         throw new JspException("Invalid URL - " + url);
      }
   }
   
   public int doEndTag() throws JspException {
      
    	// Check if the content has already been added
      if (ContentHelper.containsContent(pageContext, "RSS", _resourceName) && (!_forceInclude)) {
         release();
         return EVAL_PAGE;
      }
    	
    	JspWriter out = pageContext.getOut();
    	try {
         out.print("<link rel=\"alternate\" type=\"application/rss+xml\" title=\"");
         out.print(_title);
         out.print("\" href=\"");
         out.print(_url.toString());
         out.print("\" />");
    	} catch (Exception e) {
    	  throw new JspException(e);
    	}
    	
    	// Mark the content as added and return
    	ContentHelper.addContent(pageContext, "RSS", _url.toString());
    	release();
      return EVAL_PAGE;
   }
}