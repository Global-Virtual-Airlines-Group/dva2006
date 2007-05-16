// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import java.util.*;

import javax.servlet.jsp.JspException;

import org.deltava.beans.ComboAlias;
import org.deltava.util.CollectionUtils;

/**
 * A JSP tag to support the generation of HTML multi-option checkboxes and radio buttons.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CheckTag extends FormElementTag {

	private String _labelClassName;
	private int _width;
	private int _cols;

	private Collection _options;

	/**
	 * An additional entry prepended to the list of options
	 */
	protected Object _firstEntry;

	private String _itemSeparator;

	/**
	 * Creates a new checkbox/radio tag.
	 */
	public CheckTag() {
		super("input", true);
		setType("checkbox");
	}

	/**
	 * Sets the width of the field in columns; afterwards the separator will be outputted.
	 * @param columns the number of columns
	 * @throws IllegalArgumentException if columns is negative
	 */
	public void setCols(int columns) {
		if (columns < 0)
			throw new IllegalArgumentException("Checkbox columns cannot be negative");

		_cols = columns;
	}

	/**
	 * Sets the width of each checkbox entry.
	 * @param width the width in pixels
	 */
	public void setWidth(int width) {
		_width = width;
	}

	/**
	 * Allows an entry to be inserted before the rest of the options.
	 * @param entry the first entry to insert
	 */
	public void setFirstEntry(Object entry) {
		_firstEntry = entry;
	}

	/**
	 * Sets the JavaScript <i>onChange</i> event for these checkboxes/radio buttons.
	 * @param jsEvent the JavaScript event code
	 */
	public void setOnChange(String jsEvent) {
		_data.setAttribute("onchange", jsEvent);
	}

	private void openSpanTag() throws Exception {
		// Open the formatting SPAN and set the width
		_out.print("<span");
		if (_width > 0) {
			_out.print(" style=\"float:left; width:");
			_out.print(String.valueOf(_width));
			_out.print("px;\"");
		}

		// Set the formatting class
		if (_labelClassName != null) {
			_out.print(" class=\"");
			_out.print(_labelClassName);
			_out.print("\"");
		}

		_out.print('>');
	}

	/**
	 * Helper method to check if an option value is selected.
	 */
	private boolean checkOption(Object optValue, Object setValue) {
		if ((optValue instanceof ComboAlias) && (setValue instanceof ComboAlias)) {
			String vAlias = ((ComboAlias) setValue).getComboAlias();
			ComboAlias ca = (ComboAlias) optValue;
			return ca.getComboAlias().equals(vAlias) || ca.getComboName().equals(vAlias);
		} else if (optValue instanceof ComboAlias) {
			ComboAlias ca = (ComboAlias) optValue;
			return (ca.getComboName().equals(String.valueOf(setValue)) || ca.getComboAlias().equals(
					String.valueOf(setValue)));
		} else {
			return String.valueOf(optValue).equals(String.valueOf(setValue));
		}
	}

	/**
	 * Helper method to determine if a particular option is selected.
	 */
	private void renderOption(Object opt) throws Exception {

		// Determine if we need to include the label in a span, and if this is a comboalias
		boolean isCombo = (opt instanceof ComboAlias);
		if ((_width > 0) || (_labelClassName != null))
			openSpanTag();

		// Determine if the option is selected
		boolean isSelected = false;
		if (_value instanceof Collection) {
			for (Iterator i = ((Collection) _value).iterator(); (i.hasNext() && !isSelected);)
				isSelected = checkOption(opt, i.next());
		} else {
			isSelected = checkOption(opt, _value);
		}

		_out.print(_data.open(false));
		if (isSelected)
			_out.print(" checked=\"checked\"");

		// Figure out how to render the choice
		_out.print(" value=\"");
		if (isCombo) {
			ComboAlias alias = (ComboAlias) opt;
			_out.print(alias.getComboAlias());
			_out.print("\" />");
			_out.print(alias.getComboName());
		} else {
			_out.print(opt.toString());
			_out.print("\" />");
			_out.print(opt.toString());
		}

		// Close the span tag if required
		if ((_width > 0) || (_labelClassName != null))
			_out.println("</span>");
	}

	public int doEndTag() throws JspException {
		try {
			validateState();
			int columnCount = 0;

			// Render first option if found
			if (_firstEntry != null) {
				renderOption(_firstEntry);

				// Render the separator only after we hit the column count
				columnCount++;
				if (columnCount == _cols) {
					columnCount = 0;
					_out.println((_itemSeparator == null) ? "" : _itemSeparator);
				}
			}

			// Render the remaining options
			if (!CollectionUtils.isEmpty(_options)) {
				for (Iterator i = _options.iterator(); i.hasNext();) {
					Object opt = i.next();
					renderOption(opt);

					// Render the separator only after we hit the column count
					columnCount++;
					if (i.hasNext() && (columnCount == _cols)) {
						columnCount = 0;
						_out.println((_itemSeparator == null) ? "" : _itemSeparator);
					}
				}
			}
		} catch (Exception e) {
			throw new JspException(e);
		}

		// Release state and return
		release();
		return EVAL_PAGE;
	}

	/**
	 * Releases the tag's state data.
	 */
	public void release() {
		_firstEntry = null;
		super.release();
		setType("checkbox");
	}

	/**
	 * Sets the CSS class name <i>for the checkbox/radio text </i>.
	 * @param cName the CSS class name(s)
	 * @see ElementTag#setClassName(String)
	 */
	public final void setClassName(String cName) {
		_labelClassName = cName;
	}

	/**
	 * Sets the element type. Updats the default CSS class to &quot;check&quot; or &quot;radio&quot;.
	 * @param checkType the element type, typically RADIO or CHECKBOX
	 */
	public void setType(String checkType) {
		_data.setAttribute("type", checkType);
		_data.setAttribute("class", checkType.substring(0, 5).toLowerCase());
	}

	/**
	 * Sets the multiple element separator.
	 * @param sep the string to insert between multiple elements
	 * @see CheckTag#setNewLine(boolean)
	 */
	public void setSeparator(String sep) {
		_itemSeparator = sep;
	}
	
	/**
	 * Sets the multiple element separator to a DIV with a new line.
	 * @param doNewLine TRUE if the separator should be set, otherwise FALSE
	 * @see CheckTag#setSeparator(String)
	 */
	public void setNewLine(boolean doNewLine) {
		if (doNewLine)
			_itemSeparator = "<div style=\"clear:both;\" />";
	}

	/**
	 * Sets the selected value(s) of this element.
	 * @param values a List of selected values of this element
	 */
	public void setChecked(Collection values) {
		_value = values;
	}

	/**
	 * Sets the choices for this checkbox/radio button.
	 * @param choices a Collection of choices
	 */
	public void setOptions(Collection choices) {
		_options = (choices == null) ? Collections.emptySet() : choices;
	}
}