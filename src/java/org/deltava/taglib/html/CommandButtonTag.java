// Copyright 2005, 2006, 2007, 2015, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import javax.servlet.jsp.JspException;

import org.deltava.beans.DatabaseBean;
import org.deltava.taglib.ContentHelper;
import org.deltava.util.StringUtils;

/**
 * A JSP tag to render buttons that execute web site commands.
 * @author Luke
 * @version 9.1
 * @since 1.0 
 */

public class CommandButtonTag extends ButtonTag {

   private String _cmdName;
   private String _id;
   private String _opName;
   private boolean _doPost;

   /**
    * Releases state and readies the tag for another invocation.
    */
   @Override
   public void release() {
      super.release();
      _doPost = false;
   }

   /**
    * Sets the ID parameter for the command invocation.
    * @param id the parameter
    */
   public void setLinkID(String id) {
	   _id = id;
   }
   
	/**
	 * Sets the database ID to link to.
	 * @param db a {@link DatabaseBean} with the proper database ID
	 */
	public void setLink(DatabaseBean db) {
		if (db != null)
			_id = db.getHexID();
	}

   /**
    * Sets the command name for the button, <i>without extension </i>.
    * @param url the command name
    */
   public void setUrl(String url) {
      _cmdName = url.toLowerCase() + ".do";
   }

   /**
    * Sets the command operation for this button.
    * @param opName the operation name
    * @see org.deltava.commands.CommandContext#getCmdParameter(int, Object)
    */
   public void setOp(String opName) {
      _opName = opName;
   }

   /**
    * Sets the button to POST or GET to the form.
    * @param doPost TRUE if POSTing, FALSE if GETting
    */
   public void setPost(boolean doPost) {
      _doPost = doPost;
   }

   /**
    * Overrides the type property from the superclass. <i>NOT IMPLEMENTED </i>.
    * @throws UnsupportedOperationException
    */
   @Override
   public final void setType(String btnType) {
      throw new UnsupportedOperationException();
   }

   /**
    * Overrides the onClick property from the superclass. <i>NOT IMPLEMENTED </i>.
    * @throws UnsupportedOperationException
    */
   @Override
   public final void setOnClick(String js) {
      throw new UnsupportedOperationException();
   }

   /**
    * Overrides the className property from the superclass. <i>NOT IMPLEMENTED </i>.
    * @throws UnsupportedOperationException
    */
   @Override
   public final void setClassName(String className) {
      throw new UnsupportedOperationException();
   }

   /**
    * Renders the tag. Sets the onClick property and calls the superclass renderer.
    * @return TagSupport.EVAL_PAGE
    * @throws JspException if an error occurs
    * @throws IllegalStateException if common.js has not been added to the request
    */
   @Override
   public int doEndTag() throws JspException {

      // Ensure that the common JS file has been included
      if (!ContentHelper.containsContent(pageContext, "JS", "common"))
         throw new IllegalStateException("common.js not included in request");

      // Render the entire command string
      StringBuilder url = new StringBuilder(_cmdName);
      if (_id != null) {
         url.append("?id=");
         url.append(_id);
      }

      if (_opName != null) {
         url.append((_id == null) ? "?" : "&amp;");
         url.append("op=");
         url.append(_opName);
      }

      // Sets the JavaScript get/post function
      super.setOnClick("void golgotha.form." + ((_doPost) ? "post" : "get") + "(\'" + StringUtils.escapeQuotes(url.toString()) + "\')");
      super.setClassName("cmdButton");

      // Calls the superclass renderer
      try {
    	  return super.doEndTag();
      } finally {
    	  release();
      }
   }
}