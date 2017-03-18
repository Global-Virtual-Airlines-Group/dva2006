// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.util.StringUtils;

/**
 * A JSP tag to format Microsoft Windows version strings.
 * @author Luke
 * @version 7.3
 * @since 7.3
 */

public class WindowsVersionTag extends TagSupport {
	
	private static final String HDR = "Microsoft Windows NT ";
	private static final String SP = "Service Pack ";

	private String _version;
	
	/**
	 * Sets the Windows version.
	 * @param v the Windows version string
	 */
	public void setVersion(String v) {
		_version = v;
	}
	
	/**
	 * Renders the Windows version as a human-friendly string.
	 * @return EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		try {
			if (StringUtils.isEmpty(_version) || !_version.startsWith(HDR))
				return EVAL_PAGE;
			
			JspWriter out = pageContext.getOut();
			String s = _version.substring(HDR.length());
			int pos = s.indexOf('.', s.indexOf('.') + 1);
			if (pos == -1) {
				out.print("Unknown - ");
				out.print(s);
				return EVAL_PAGE;
			}
			
			out.print("Windows ");
			String ver = s.substring(0, pos);
			switch (ver) {
			case "10.0":
				String[] vers = s.split("[[.]]");
				out.print("10.");
				out.print(vers[1]);
				if (vers.length > 2) {
					out.print(" (Build ");
					out.print(vers[2]);
					out.print(')');
				}
				
				break;
				
			case "6.3":
				out.print("8.1");
				break;
				
			case "6.2":
				out.print('8');
				break;
				
			case "6.1":
				out.print('7');
				break;
				
			case "6.0":
				out.print("Vista");
				break;
			
			case "5.2":
				out.print("XP64");
				break;
				
			case "5.1":
				out.print("XP");
				break;
				
			default:
				out.print(s.substring(0, s.indexOf(' ')));
			}
				
			int sppos = s.indexOf(SP);
			if (sppos < 0)
				return EVAL_PAGE;
			
			// Parse service pack
			String sp = s.substring(sppos + SP.length());
			if (sp.length() > 0) {
				out.print(" Service Pack ");
				out.print(sp.substring(0, 1));
			}
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}
		
		return EVAL_PAGE;
	}
}