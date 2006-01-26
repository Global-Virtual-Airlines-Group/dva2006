// Copyright (c) 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import java.util.*;

import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;
import javax.servlet.jsp.JspException;

/**
 * A JSP tag to create a link to a web site command.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CommandLinkTag extends LinkTag {

	private String _cmdName;
	private Map<String, String> _cmdParams = new TreeMap<String, String>();
	private boolean _disableLink;

	/**
	 * Sets the ID parameter for the command invocation. If it starts with &quot;0x&quot; then turn the rest of the
	 * string into a hexadecimal number string.
	 * @param id the parameter
	 * @see org.deltava.commands.CommandContext#getCmdParameter(int, Object)
	 */
	public void setLinkID(String id) {
		if (id.startsWith("0x")) {
			if (!"0x0".equals(id)) {
				try {
					_cmdParams.put("id", "0x" + Integer.toString(Integer.parseInt(id.substring(2)), 16).toUpperCase());
				} catch (NumberFormatException nfe) {
					_cmdParams.put("id", id);
				}
			} else {
				_disableLink = true;
			}
		} else {
			_cmdParams.put("id", id);
		}
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