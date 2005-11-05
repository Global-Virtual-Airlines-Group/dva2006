package org.deltava.taglib.format;

import java.util.*;
import java.lang.reflect.*;
import java.io.IOException;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

import org.deltava.util.StringUtils;

/**
 * A JSP Tag to concatenate bean properties into a delimited string. 
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class BeanListFormatTag extends TagSupport {

	private Collection _values;
	private String _propertyName;
	private String _delim;
	private boolean _ignoreErrors;
	
	/**
	 * Sets the name of the property to retrieve.
	 * @param pName the property name
	 */
	public void setProperty(String pName) {
		_propertyName = StringUtils.getPropertyMethod(pName); 
	}
	
	/**
	 * Sets the delimiter between property values.
	 * @param delim the delimiter
	 */
	public void setDelim(String delim) {
		_delim = delim;
	}
	
	/**
	 * Sets the values to extract the property value from.
	 * @param values a Collection of beans
	 */
	public void setValue(Collection values) {
		_values = values;
	}
	
	/**
	 * Sets the flag to ignore rendering errors. If this is not set, if any value in the values Collection
	 * does not support the specified property, an error will be thrown.
	 * @param ignore TRUE if rendering errors should be ignored, otherwise FALSE
	 */
	public void setIgnoreErrors(boolean ignore) {
		_ignoreErrors = ignore;
	}
	
	/**
	 * Resets the tag's state variables.
	 */
	public void release() {
		super.release();
		_ignoreErrors = false;
	}
	
	/**
	 * Renders the delimited list of properties to the JSP output stream. If any bean in the values
	 * Collection does not implement the specified property, an exception will be thrown, unless
	 * the ignoreErrors flag is true.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if an I/O error occurs, or an entry in the values Collection does not
	 * contain the specified property
	 */
	public int doEndTag() throws JspException {

		// Get the values and append them to the list
		StringBuffer buf = new StringBuffer();
		for (Iterator i = _values.iterator(); i.hasNext(); ) {
			Object value = getPropertyValue(i.next());
			if (value != null) {
				buf.append(StringUtils.stripInlineHTML(String.valueOf(value)));
				if (i.hasNext())
					buf.append(_delim);
			}
		}

		// Write the list to the JSP output stream
		try {
			pageContext.getOut().write(buf.toString());
		} catch (IOException ie) {
			throw new JspException(ie);
		}

		release();
		return EVAL_PAGE;
	}
	
	/**
	 * Helper method to retun the value of a particular bean property.
	 */
	private Object getPropertyValue(Object obj) throws JspException {
		
		// Get the method and invoke it on the object
		try {
			Method m = obj.getClass().getMethod(_propertyName, (Class []) null);
			return m.invoke(obj, (Object []) null);
		} catch (NoSuchMethodException nsme) {
			if (!_ignoreErrors)
				throw new JspException(obj.getClass() + " has no method " + _propertyName + "()");
		} catch (InvocationTargetException ite) {
			if (!_ignoreErrors)
				throw new JspException("Error invoking " + obj.getClass() + "." + _propertyName + "()");
		} catch (Exception e) {
			if (!_ignoreErrors)
				throw new JspException(e);
		}
		
		// This will be invoked if _ignoreErrors is true, just return NULL for an error
		return null;
	}
}