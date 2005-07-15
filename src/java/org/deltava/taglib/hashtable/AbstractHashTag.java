package org.deltava.taglib.hashtable;

import java.util.*;
import java.lang.reflect.Method;

import java.io.IOException;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A class to support JSP tags that generate JavaScript hashtables of arrays.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class AbstractHashTag extends TagSupport {

	/**
	 * Shortcut to the JavaScript array to reduce output size.
	 */
	protected static final String ARRAY_NAME = "valArray";
	
	private String _hashName;
	private String _keyMethod;
	private String _valueMethod;
	private Comparator _cmp;

	private Set _validKeys;
	
	/**
	 * The items to split into the hashtable arrays.
	 */
	protected Collection _items;
	
	/**
	 * The component parts of the hashtable. This is protected since it is up to each implementing
	 * subclass to render these values to the JSP output stream.
	 */
	protected Map _sets = new HashMap();
	
	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		super.release();
		_sets.clear();
		_cmp = null;
		_valueMethod = null;
		_validKeys = null;
	}
	
	/**
	 * Sets the name of the JavaScript hashtable variable.
	 * @param varName the variable name
	 */
	public void setVar(String varName) {
		_hashName = varName;
	}
	
	/**
	 * Sets the sorter for each hashtable's items.
	 * @param cmp the Comparator to sort the items. NULL indicates natural sorting
	 */
	public void setSorter(Comparator cmp) {
		_cmp = cmp;
	}
	
	/**
	 * Sets the items to be sliced into the different hashmaps of arrays.
	 * @param beans a Collection of beans
	 */
	public void setItems(Collection beans) {
		_items = beans;
	}
	
	/**
	 * Sets the property used to fetch the key value(s) for each item.
	 * @param methodName the property name to be converted to a getXXX() method name
	 * @see AbstractHashTag#setValue(String) 
	 */
	public void setKey(String methodName) {
		StringBuffer buf = new StringBuffer("get");
		buf.append(Character.toUpperCase(methodName.charAt(0)));
		buf.append(methodName.substring(1));
		_keyMethod = buf.toString();
	}
	
	/**
	 * Sets the property used to fetch the value(s) for each item.
	 * @param methodName the property name to be converted to a getXXX() method name. If
	 * this parameter is not specified, the item itself will be the value
	 * @see AbstractHashTag#setKey(String)
	 */
	public void setValue(String methodName) {
		StringBuffer buf = new StringBuffer("get");
		buf.append(Character.toUpperCase(methodName.charAt(0)));
		buf.append(methodName.substring(1));
		_valueMethod = buf.toString();
	}
	
	/**
	 * Adds a new item to the hashmap arrays.
	 * @param key the key value
	 * @param value the object
	 */
	protected void addItem(String key, Object value) {
		
		// Check if the key already exists, if we have validKeys defined check using them too
		if (!_sets.containsKey(key)) {
			if ((_validKeys == null) || (_validKeys.contains(key)))
				_sets.put(key, new TreeSet(_cmp));
		}
		
		// Add the value to the set
		Set values = (Set) _sets.get(key);
		values.add(value);
	}
	
	/**
	 * Adds a key to the list of valid keys, used to filter the number of arrays in the hashtable. 
	 * @param validKey the key
	 */
	protected void addValidKey(String validKey) {
		if (_validKeys == null)
			_validKeys = new HashSet();
		
		_validKeys.add(validKey);
	}
	
	/**
	 * Writes the JavaScript opening block to the JSP writer.
	 * @param out the JSP writer
	 * @throws IOException if an I/O error occurs
	 * @see AbstractHashTag#closeScriptBlock(JspWriter)
	 * @see AbstractHashTag#renderArrayStart(JspWriter, String, int)
	 */
	protected void openScriptBlock(JspWriter out) throws IOException {
		out.println("<SCRIPT LANGUAGE=\"JavaScript\" TYPE=\"text/javascript\">");
		out.println("var " + _hashName + " = {};");
	}
	
	/**
	 * Writes the JavaScript closing block to the JSP writer.
	 * @param out the JSP writer
	 * @throws IOException if an I/O error occurs
	 * @see AbstractHashTag#openScriptBlock(JspWriter)
	 * @see AbstractHashTag#renderArrayStart(JspWriter, String, int)
	 */
	protected void closeScriptBlock(JspWriter out) throws IOException {
		out.println("</SCRIPT>");
	}
	
	/**
	 * Writes an array definition to the JSP writer.
	 * @param out the JSP writer
	 * @param key the key value
	 * @throws IOException if an I/O error occurs
	 * @see AbstractHashTag#openScriptBlock(JspWriter)
	 * @see AbstractHashTag#closeScriptBlock(JspWriter)
	 */
	protected void renderArrayStart(JspWriter out, String key, int size) throws IOException {
		out.println("\n// Entries for key " + key + " - " + size + " entries");
		out.println(_hashName + "[\"" + key + "\"] = new Array(" + size + ");");
		out.println(ARRAY_NAME + " = " + _hashName + "[\"" + key + "\"];");
	}
	
	/**
	 * Gets the keys for a given object by calling the key property.
	 * @param obj the object to query
	 * @return a sorted Set of key objects, or an empty Set if an error occurs
	 */
	private Set getKeys(Object obj) {
		try {
			Class c = obj.getClass();
			Method m = c.getMethod(_keyMethod, null);
			
			// Get the keys from the object
			Object keys = m.invoke(obj, null);
			Set results = new TreeSet();
			if (keys instanceof Collection) {
				results.addAll((Collection) keys);
			} else {
				results.add(String.valueOf(keys));
			}
			
			// If we have validKeys, then filter based on them
			if (_validKeys != null) {
				for (Iterator i = results.iterator(); i.hasNext(); ) {
					Object key = (String) i.next();
					if (!_validKeys.contains(key))
						i.remove();
				}
			}
			
			return results;
		} catch (Exception e) {
			return Collections.EMPTY_SET;
		}
	}

	/**
	 * Returns the values for a given object by calling the value property.
	 * @param obj the object to query
	 * @return the value, or the object itself if no value property specified
	 */
	private Object getValue(Object obj) {
		// If no value method was specified, return the entire bean
		if (_valueMethod == null)
			return obj;
		
		try {
			Class c = obj.getClass();
			Method m = c.getMethod(_valueMethod, null);
			
			return m.invoke(obj, null);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * This method takes the items, extracts their keys and adds them to the different hashmap
	 * arrays. This could be contained within doEndTag() but is placed here for convenience.
	 * Implementing subclasses that wish to override this method may do so without calling the
	 * <b>super</b> implementation. 
	 */
	public int doStartTag() throws JspException {
		
		for (Iterator i = _items.iterator(); i.hasNext(); ) {
			Object item = i.next();
			
			// Add the value to the hashmaps for each key
			for (Iterator ki = getKeys(item).iterator(); ki.hasNext(); ) {
				String key = (String) ki.next();
				addItem(key, getValue(item));
			}
		}
		
		return SKIP_BODY;
	}
}