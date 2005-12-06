package org.deltava.taglib.html;

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
	private String _cmdParam;
	private String _cmdOpName;

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
					_cmdParam = "0x" + Integer.toString(Integer.parseInt(id.substring(2)), 16).toUpperCase();
				} catch (NumberFormatException nfe) {
					_cmdParam = id;
				}
			} else {
				_disableLink = true;
			}
		} else {
			_cmdParam = id;
		}
	}

	/**
	 * Sets the operation parameter for the command invocation.
	 * @param opName the operation name
	 */
	public void setOp(String opName) {
		_cmdOpName = opName;
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
		_cmdParam = null;
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
			if (_cmdParam != null) {
				url.append("?id=");
				url.append(URLEncoder.encode(_cmdParam, "UTF-8"));
			}

			if (_cmdOpName != null) {
				url.append((_cmdParam == null) ? "?" : "&amp;");
				url.append("op=");
				url.append(URLEncoder.encode(_cmdOpName, "UTF-8"));
			}

			// Update the HREF and call the superclass renderer
			_attrs.put("href", url.toString());
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