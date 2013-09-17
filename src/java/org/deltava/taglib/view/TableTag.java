// Copyright 2005, 2013 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.view;

import javax.servlet.jsp.JspException;

import org.deltava.util.system.SystemData;

/**
 * A JSP tag to support database view tables.
 * @author Luke
 * @version 5.1
 * @since 1.0
 */

public class TableTag extends org.deltava.taglib.html.TableTag {

   private String _viewCommandName;
   private int _viewSize = SystemData.getInt("html.table.viewSize");

   /**
    * Sets the Web Site Command to execute when scrolling through the view.
    * @param name the command ID
    * @see TableTag#getCmd()
    */
   public void setCmd(String name) {
      _viewCommandName = name.toLowerCase();
   }

   /**
    * Sets the number of view rows to display.
    * @param size the number of rows
    * @see TableTag#size()
    */
   public void setSize(int size) {
      if (size > 0)
         _viewSize = size;
   }
   
   /**
    * Renders the tag to the JSP output stream.
    * @return EVAL_PAGE
    * @throws JspException if an error occurs
    */
   @Override
   public int doStartTag() throws JspException {
	   setClassName("view");
	   return super.doStartTag();
   }
   
   /**
    * Renders the tag to the JSP output stream.
    * @return EVAL_PAGE
    * @throws JspException if an error occurs
    */
   @Override
   public int doEndTag() throws JspException {
	   try {
		   return super.doEndTag();
	   } finally {
		   release();
	   }
   }
   
   /**
    * Releases the tag's state variables.
    */
   @Override
   public void release() {
      super.release();
      _viewSize = SystemData.getInt("html.table.viewSize");
   }

   /**
    * Returns the view command ID (for unit tests).
    * @return the command ID
    * @see TableTag#setCmd(String)
    */
   protected String getCmd() {
      return _viewCommandName;
   }

   /**
    * Returns the view scroll window size (for unit tests).
    * @return the number of rows
    * @see TableTag#setSize(int)
    */
   protected int size() {
      return _viewSize;
   }
}