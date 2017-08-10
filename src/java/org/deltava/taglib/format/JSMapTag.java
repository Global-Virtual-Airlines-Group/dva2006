// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.format;

import java.util.*;

import javax.servlet.jsp.JspException;

import org.json.*;

import org.deltava.taglib.JSTag;

/**
 * A JSP tag to add objects into a JavaScript associative array. 
 * @author Luke
 * @version 7.5
 * @since 7.5
 */

public class JSMapTag extends JSTag {

	private Map<String, Object> _data;
	
	/**
	 * Sets the items to put into the JavaScript array.
	 * @param items a Collection of objects.
	 */
	public void setObject(Map<String, Object> items) {
		_data = items;
	}
	
	/**
	 * Renders the JavaScript object to the JSP output stream.
	 * @return EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		
		JSONObject jo = new JSONObject();
		for (Map.Entry<String, Object> me : _data.entrySet()) {
			if (me.getValue() instanceof Collection) {
				JSONArray ja = new JSONArray();
				
				@SuppressWarnings("unchecked")
				Collection<Object> co = (Collection<Object>) me.getValue();
				for (Object o : co) {
					if (o instanceof Double)
						ja.put(((Double) o).doubleValue());
					else if (o instanceof Integer)
						ja.put(((Integer) o).intValue());
					else if (o instanceof Long)
						ja.put(((Long) o).longValue());
					else if (o instanceof Boolean)
						ja.put(((Boolean) o).booleanValue());
					else
						ja.put(String.valueOf(o));
				}
				
				jo.put(me.getKey(), ja);
			} else if (me.getValue() instanceof Double)
				jo.put(me.getKey(), ((Double)me.getValue()).doubleValue());
			else if (me.getValue() instanceof Long)
				jo.put(me.getKey(), ((Long)me.getValue()).longValue());
			else if (me.getValue() instanceof Integer)
				jo.put(me.getKey(), ((Integer)me.getValue()).intValue());
			else if (me.getValue() instanceof Boolean)
				jo.put(me.getKey(), ((Boolean)me.getValue()).booleanValue());
			else
				jo.put(me.getKey(), String.valueOf(me.getValue()));
		}
		
		try {
			writeVariableName();
			pageContext.getOut().write(jo.toString());
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();
		}

		return EVAL_PAGE;
	}
}