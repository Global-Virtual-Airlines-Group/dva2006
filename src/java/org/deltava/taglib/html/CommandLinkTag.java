// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import java.text.*;
import java.util.*;

import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;
import javax.servlet.jsp.JspException;

import org.deltava.beans.DatabaseBean;
import org.deltava.util.StringUtils;

/**
 * A JSP tag to create a link to a Web Site Command.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CommandLinkTag extends LinkTag {

	private String _cmdName;
	private final Map<String, String> _cmdParams = new TreeMap<String, String>();
	private boolean _disableLink;

	/**
	 * Sets the ID parameter for the command invocation.
	 * @param id the parameter
	 */
	public void setLinkID(String id) {
		if (StringUtils.isEmpty(id))
			_disableLink = true;
		else
			_cmdParams.put("id", id);
	}
	
	/**
	 * Sets the database ID to link to.
	 * @param db a {@link DatabaseBean} with the proper database ID
	 */
	public void setLink(DatabaseBean db) {
		if (db != null)
			_cmdParams.put("id", db.getHexID());
	}
	
	/**
	 * Sets the operation parameter for the command invocation.
	 * @param opName the operation name
	 */
	public void setOp(String opName) {
		_cmdParams.put("op", opName);
	}
	
	/**
	 * Sets the sort parameter for the command invocation.
	 * @param sortType the sort type
	 */
	public void setSort(String sortType) {
		_cmdParams.put("sortType", sortType);
	}

	/**
	 * Sets the command name.
	 * @param url the command name
	 */
	public void setUrl(String url) {
		_cmdName = url.toLowerCase() + ".do";
	}
	
	/**
	 * Sets the start date if this is linking to a Calendar command.
	 * @param dt the calendar start date/time
	 */
	public void setStartDate(Date dt) {
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		_cmdParams.put("startDate", df.format(dt));
	}
	
	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		super.release();
		_disableLink = false;
		_cmdParams.clear();
	}

	/**
	 * Renders the start of the HREF tag to the JSP output stream. The HREF tag will not be rendered
	 * if the linkID parameter is &quot;0x0&quot;.
	 * @return TagSupport.EVAL_BODY_INCLUDE
	 * @throws JspException if an error occurs 
	 */
	public final int doStartTag() throws JspException {
		// Do nothing if disable flag set
		if (_disableLink)
			return EVAL_BODY_INCLUDE;
		
		StringBuilder url = new StringBuilder(_cmdName);
		try {
			if (!_cmdParams.isEmpty())
				url.append('?');
			
			// Append the parameters
			for (Iterator<String> i = _cmdParams.keySet().iterator(); i.hasNext(); ) {
				String pName = i.next();
				url.append(pName);
				url.append('=');
				url.append(URLEncoder.encode(_cmdParams.get(pName), "UTF-8"));
				if (i.hasNext())
					url.append("&amp;");
			}

			// Update the HREF and call the superclass renderer
			_data.setAttribute("href", url.toString());
		} catch (UnsupportedEncodingException uee) {
			throw new JspException("UTF-8 encoding not supported - Laws of Universe no longer apply");
		}

		return super.doStartTag();
	}

	/**
	 * Renders the end of the HREF tag to the JSP output stream.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if an error occurs
	 */
	public int doEndTag() throws JspException {
		if (!_disableLink)
			super.doEndTag();
		
		release();
		return EVAL_PAGE;
	}
}