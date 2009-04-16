// Copyright 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

import java.util.Date;

import com.feldt.metar.Metar;
import com.feldt.metar.exceptions.MetarParseException;

/**
 * A bean to store airport METAR data.
 * @author Luke
 * @version 2.5
 * @since 2.2
 */

public class METAR extends WeatherDataBean {
	
	private Metar _metar;
	
	/**
	 * Sets the expiration date of the bean (15 minutes after effective date).
	 */
	public Date getExpiryDate() {
		return new Date(getDate().getTime() + 900000);
	}
	
	public String getIconColor() {
		return WHITE;
	}
	
	public String getType() {
		return "METAR";
	}
	
	/**
	 * Sets and parses the METAR data.
	 */
	public void setData(String data) {
		super.setData(data);
		try {
			_metar = Metar.parse(_pos.getCode(), data, "");
		} catch (MetarParseException mpe) {
			
		}
	}

	public int hashCode() {
		return cacheKey().hashCode();
	}
}