// Copyright 2005, 2006, 2009, 2012, 2013, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.html;

import java.security.Principal;
import java.util.*;
import java.io.IOException;

import javax.servlet.jsp.*;
import javax.servlet.http.HttpServletRequest;

import org.deltava.beans.*;
import org.deltava.beans.schedule.Airport;
import org.deltava.util.StringUtils;

/**
 * A JSP tag to support generating HTML combo/list boxes.
 * @author Luke
 * @version 11.0
 * @since 1.0
 */

public class ComboTag extends FormElementTag {

	private Collection<?> _options;
	private Object _firstEntry;
	private Airport.Code _airportCode;

	/**
	 * Create a new combo/listbox tag.
	 */
	public ComboTag() {
		super("select", false);
	}

	/*
	 * Helper method to check if an option value is selected.
	 */
	private static boolean checkOption(Object optValue, Object setValue) {
		if ((optValue instanceof ComboAlias ca) && (setValue instanceof ComboAlias sca)) {
			String vAlias = sca.getComboAlias();
			return ca.getComboAlias().equals(vAlias) || ca.getComboName().equals(vAlias);
		} else if (optValue instanceof ComboAlias ca)
			return (ca.getComboName().equals(String.valueOf(setValue)) || ca.getComboAlias().equals(String.valueOf(setValue)));
		else
			return String.valueOf(optValue).equals(String.valueOf(setValue));
	}

	/*
	 * Helper method render an option to the JSP output stream.
	 */
	private void renderOption(Object optValue) throws IOException {

		// Determine if the option is selected
		boolean isSelected = false;
		if (_value instanceof Collection<?>) {
			for (Iterator<?> i = ((Collection<?>) _value).iterator(); (i.hasNext() && !isSelected);)
				isSelected = checkOption(optValue, i.next());
		} else
			isSelected = checkOption(optValue, _value);

		// Figure out how to render the choice
		if (optValue instanceof ComboAlias alias) {
			_out.print(" <option ");
			
			// Special airport attributes
			String txt = null;
			if (optValue instanceof Airport a) {
				_out.print("class=\"airport\" iata=\"");
				_out.print(a.getIATA());
				_out.print("\" icao=\"");
				_out.print(a.getICAO());
				_out.print("\" ");

				// Generate text
				StringBuilder buf = new StringBuilder(a.getName());
				buf.append(" (");
				buf.append((_airportCode == Airport.Code.IATA) ? a.getIATA() : a.getICAO());
				buf.append(')');
				txt = buf.toString();
			} else
				txt = StringUtils.stripInlineHTML(alias.getComboName());

			if (isSelected)
				_out.print("selected=\"selected\" ");
			
			_out.print("value=\"");
			_out.print(alias.getComboAlias());
			_out.print("\">");
			_out.print(txt);
		} else {
			_out.print(" <option");
			_out.print(isSelected ? " selected=\"selected\">" : ">");
			_out.print(StringUtils.stripInlineHTML(optValue.toString()));
		}

		// Close the option
		_out.println("</option>");
	}

	/**
	 * Loads airport formatting preferences from the logged in user.
	 */
	@Override
	public void setPageContext(PageContext ctxt) {
		super.setPageContext(ctxt);
		HttpServletRequest req = (HttpServletRequest) ctxt.getRequest();
		Principal user = req.getUserPrincipal();
		if (user instanceof Person p)
			_airportCode = p.getAirportCodeType();
	}

	/**
	 * Generates the combo/listbox by writing a &gt;SELECT&lt; tag, rendering all choices as &gt;OPTION&lt; elements,
	 * then writing a &gt;/SELECT&lt; tag.
	 * @throws JspException if an I/O error occurs
	 */
	@Override
	public int doEndTag() throws JspException {
		try {
			validateState();
			_out.println(_data.open(true));

			// Render the first entry if present
			if (_firstEntry != null)
				renderOption(_firstEntry);

			// Render the options
			for (Object opt : _options) {
				if (opt != null)
					renderOption(opt);
			}

			_out.println(_data.close());
		} catch (Exception e) {
			throw new JspException(e);
		} finally {
			release();	
		}

		return EVAL_PAGE;
	}

	/**
	 * Sets the value(s) of this combo/listbox.
	 * @param values a Collection of selected values
	 */
	public void setValues(Collection<?> values) {
		_value = values;
	}

	/**
	 * Sets the size of this combo/list box. This does nothing if a negative, zero or non-numeric value is passed.
	 * @param size the size of the combo/list box, in entries
	 * @see ElementTag#setNumericAttr(String, int, int)
	 */
	public void setSize(int size) {
		setNumericAttr("size", size, 1);
	}

	/**
	 * Sets the Javascript to execute when this element's value is changed.
	 * @param js the JavaScript code to execute
	 */
	public void setOnChange(String js) {
		_data.setAttribute("onchange", js);
	}
	
	/**
	 * Sets the Javascript to execute when the element is right-clicked.
	 * @param js the JavaScript code to execute
	 */
	public void setOnRightClick(String js) {
		_data.setAttribute("oncontextmenu", js);
	}

	/**
	 * Allows an entry to be inserted before the rest of the options.
	 * @param entry the first entry to insert
	 */
	public void setFirstEntry(Object entry) {
		_firstEntry = entry;
	}

	/**
	 * Marks this as a listbox (allowing multiple entries).
	 * @param allowMulti TRUE if enabled, otherwise FALSE
	 */
	public void setMultiple(boolean allowMulti) {
		if (allowMulti)
			_data.setAttribute("multiple", "multiple");
	}

	/**
	 * Sets the choices for this combo/listbox.
	 * @param choices a List of choices
	 */
	public void setOptions(Collection<?> choices) {
		_options = (choices == null) ? Collections.emptySet() : choices;
	}

	@Override
	public void release() {
		super.release();
		_firstEntry = null;
		_airportCode = Airport.Code.IATA;
	}
}