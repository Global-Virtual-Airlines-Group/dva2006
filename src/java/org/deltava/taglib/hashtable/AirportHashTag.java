package org.deltava.taglib.hashtable;

import java.security.Principal;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.*;

import org.deltava.beans.Person;
import org.deltava.beans.schedule.Airline;
import org.deltava.beans.schedule.Airport;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A JSP tag to store airport OPTION data in a JavaScript hashtable of arrays.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class AirportHashTag extends AbstractHashTag {
	
	private int _codeType = Airport.IATA;

	/**
	 * Sets the airline to load airports for.
	 * @param aCode the case-insensitive airline code, comma-delimited if multiple values
	 * @throws IllegalArgumentException if an invalid airline code was specified
	 */
	public void setAirline(String aCode) {
		StringTokenizer tkns = new StringTokenizer(aCode.toUpperCase(), ",");
		Map airlines = (Map) SystemData.getObject("airlines");

		// Parse the tokens
		while (tkns.hasMoreTokens()) {
			String code = tkns.nextToken();
			if (airlines.containsKey(code)) {
				addValidKey(code);
			} else {
				throw new IllegalArgumentException("Airline Code " + code + " not found");
			}
		}
	}
	
	/**
	 * Sets the airlines to load airports for. This can take a Collection of the Airline beans themselves
	 * or a Collection of airline codes.
	 * @param acList a Collection of airline beans/codes
	 * @throws IllegalArgumentException if an invalid airline code is specified
	 * @see AirportHashTag#setAirline(String)
	 */
	public void setAirlines(Collection acList) {
		for (Iterator i = acList.iterator(); i.hasNext(); ) {
			Object ac = i.next();
			if (ac instanceof String) {
				setAirline((String) ac);
			} else if (ac instanceof Airline) {
				Airline a = (Airline) ac;
				setAirline(a.getCode());
			}
		}
	}
	
    /**
     * Sets the tag's JSP context and loads the code type to display from the user's preferences.
     * @param ctxt the JSP context
     * @see Person#getAirportCodeType()
     */
    public final void setPageContext(PageContext ctxt) {
        super.setPageContext(ctxt);
        HttpServletRequest req = (HttpServletRequest) ctxt.getRequest();
        Principal user = req.getUserPrincipal();
        if (user instanceof Person) {
            Person p = (Person) user;
            _codeType = p.getAirportCodeType();
        }
    }
	
	/**
	 * Releases the tag's state variables.
	 */
	public void release() {
		super.release();
		_codeType = Airport.IATA;
	}
	
	/**
	 * Splits up the airports into a Map of Sets of airports sorted by name. This is done in the start
	 * tag to split up the code between data processing and rendering - this could all be handled in
	 * the doEndTag() method.
	 * @return TagSupport.SKIP_BODY
	 * @throws JspException if an error occurs
	 */
	public int doStartTag() throws JspException {
		
		// Check if we've included the airportRefresh JavaScript file
		if (!containsContent("JS", "airportRefresh"))
			throw new IllegalStateException("airportRefresh.js must have been included");
		
		// Set the key property and split the hashtable
		setKey("airlineCodes");
		return super.doStartTag();
	}
	
	/**
	 * Renders the hashtable to the JSP output stream. This will output a JavaScript block and automatically
	 * open and close the &lt;SCRIPT&gt; element.
	 * @return TagSupport.EVAL_PAGE
	 * @throws JspException if an error occurs
	 */
	public int doEndTag() throws JspException {
		
		JspWriter out = pageContext.getOut();
		try {
			openScriptBlock(out);
			
			// Loop through the airline codes
			for (Iterator i = _sets.keySet().iterator(); i.hasNext(); ) {
				String aCode = (String) i.next();
				Set aSet = (Set) _sets.get(aCode);
				
				// Write the array definition
				renderArrayStart(out, aCode, aSet.size());
				
				// Write an empty airport
				out.println(ARRAY_NAME + "[0] = new Option(\'< SELECT AIRPORT >\', \'\');");
				
				// Get all the airports in the set, dump them to the output stream as OPTION elements
				int idx = 1;
				for (Iterator i2 = aSet.iterator(); i2.hasNext(); ) {
					Airport a = (Airport) i2.next();
					out.print(ARRAY_NAME + "[" + idx + "] = new Option(\'" + StringUtils.stripInlineHTML(a.getComboName()) + "\', \'");
					if (_codeType == Airport.ICAO) {
						out.print(a.getICAO());
					} else {
						out.print(a.getIATA());
					}

					out.println("\');");
					idx++;
				}
			}
			
			closeScriptBlock(out);
		} catch (Throwable t) {
	        throw new JspException("Error writing " + getClass().getName(), t);
		}
		
		return EVAL_PAGE;
	}
}