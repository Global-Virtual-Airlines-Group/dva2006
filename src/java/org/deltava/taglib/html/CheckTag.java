// Copyright 2005, 2006, 2007, 2009, 2010, 2012, 2015, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import java.util.*;

import javax.servlet.jsp.JspException;

import org.deltava.beans.ComboAlias;
import org.deltava.util.CollectionUtils;

/**
 * A JSP tag to support the generation of HTML multi-option checkboxes and radio buttons.
 * @author Luke
 * @version 11.0
 * @since 1.0
 */

public class CheckTag extends FormElementTag {

	private String _fieldName;
	private String _labelClassName;
	private int _width;
	private int _cols = Integer.MAX_VALUE;

	private Collection<?> _options;

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
	 */
	public void setCols(int columns) {
		_cols = Math.max(1, columns);
	}

	/**
	 * Sets the width of each checkbox entry.
	 * @param width the width in pixels
	 */
	public void setWidth(int width) {
		_width = width;
	}
	
	/**
	 * Sets the check box name, saving for the CSS class.
	 */
	@Override
	public void setName(String name) {
		super.setName(name);
		_fieldName = name;
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

	/*
	 * Open the formatting SPAN and set the width.
	 */
	private void openSpanTag() throws Exception {
		_out.print("<span");
		if (_width > 0) {
			_out.print(" style=\"");
			if (_cols > 1)
				_out.print("float:left; ");
			
			_out.print("width:");
			_out.print(String.valueOf(_width));
			_out.print("px;\"");
		}

		// Set the formatting class
		_out.print(" class=\"cbox_span_");
		_out.print(_fieldName);
		if (_labelClassName != null) {
			_out.print(' ');	
			_out.print(_labelClassName);
		}

		_out.print("\">");
	}

	/*
	 * Helper method to check if an option value is selected.
	 */
	private static boolean checkOption(Object optValue, Object setValue) {
		if ((optValue instanceof ComboAlias ca) && (setValue instanceof ComboAlias sva)) {
			String vAlias = sva.getComboAlias();
			return ca.getComboAlias().equals(vAlias) || ca.getComboName().equals(vAlias);
		} else if (optValue instanceof ComboAlias ca)
			return (ca.getComboName().equals(String.valueOf(setValue)) || ca.getComboAlias().equals(String.valueOf(setValue)));
		else
			return String.valueOf(optValue).equals(String.valueOf(setValue));
	}

	/*
	 * Helper method to determine if a particular option is selected.
	 */
	private void renderOption(Object opt) throws Exception {
		openSpanTag();

		// Determine if the option is selected
		boolean isSelected = false;
		if (_value instanceof Collection<?> vc) {
			for (Iterator<?> i = vc.iterator(); (i.hasNext() && !isSelected);)
				isSelected = checkOption(opt, i.next());
		} else
			isSelected = checkOption(opt, _value);

		_out.print(_data.open(false));
		_out.print(" class=\"cbox_input_");
		_out.print(_fieldName);
		_out.print('\"');
		if (isSelected)
			_out.print(" checked=\"checked\"");

		// Figure out how to render the choice
		_out.print(" value=\"");
		if (opt instanceof ComboAlias alias) {
			_out.print(alias.getComboAlias());
			_out.print("\" />");
			_out.print(alias.getComboName());
		} else {
			_out.print(opt.toString());
			_out.print("\" />");
			_out.print(opt.toString());
		}

		_out.println("</span>");
	}

	/**
	 * Renders the checkbox tags.
	 * @return EVAL_PAGE always
	 * @throws JspException if an error occurs
	 */
	@Override
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
				for (Iterator<?> i = _options.iterator(); i.hasNext();) {
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
		} finally {
			release();
		}

		return EVAL_PAGE;
	}

	/**
	 * Releases the tag's state data.
	 */
	@Override
	public void release() {
		super.release();
		_firstEntry = null;
		_cols = Integer.MAX_VALUE;
		setType("checkbox");
	}

	/**
	 * Sets the CSS class name <i>for the checkbox/radio text </i>.
	 * @param cName the CSS class name(s)
	 * @see ElementTag#setClassName(String)
	 */
	@Override
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
			_itemSeparator = "<div style=\"clear:both;\"></div>";
	}

	/**
	 * Sets the selected value(s) of this element.
	 * @param values a List of selected values of this element
	 */
	public void setChecked(Collection<Object> values) {
		_value = values;
	}

	/**
	 * Sets the choices for this checkbox/radio button.
	 * @param choices a Collection of choices
	 */
	public void setOptions(Collection<?> choices) {
		_options = (choices == null) ? Collections.emptySet() : choices;
	}
}