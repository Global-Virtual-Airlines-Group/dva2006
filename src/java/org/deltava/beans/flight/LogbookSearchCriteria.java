// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import org.deltava.beans.schedule.*;

/**
 * A bean to store Pilot logbook search criteria.
 * @author Luke
 * @version 10.1
 * @since 10.1
 */

public class LogbookSearchCriteria implements RoutePair {
	
	private final String _sortBy;
	private final String _dbName;
	private boolean _loadComments;
	
	private String _eqType;
    private Airport _airportD;
    private Airport _airportA;
	
	/**
	 * Creates the bean.
	 * @param sortBy the SQL sorting clause 
	 * @param dbName the database name
	 */
	public LogbookSearchCriteria(String sortBy, String dbName) {
		super();
		_sortBy = sortBy;
		_dbName = dbName;
	}
	
	/**
	 * Returns whether Flight Report comments should be loaded. This may have a performance impact on large logbooks.
	 * @return TRUE if comments should be loaded, otherwise FALSE
	 */
	public boolean getLoadComments() {
		return _loadComments;
	}
	
	/**
	 * Returns the database to search.
	 * @return the database name
	 */
	public String getDBName() {
		return _dbName;
	}

	/**
	 * Returns the SQL sorting clause.
	 * @return the sorting SQL
	 */
	public String getSortBy() {
		return _sortBy;
	}
	
    /**
     * Returns the Equipment type to search for.
     * @return the equipment code
     */
    public String getEquipmentType() {
        return _eqType;
    }

    @Override
    public Airport getAirportA() {
        return _airportA;
    }

    @Override
    public Airport getAirportD() {
        return _airportD;
    }

	/**
	 * Updates whether Flight Report comments should be loaded. This may have a performance impact on large logbooks.
	 * @param loadComments TRUE if comments should be loaded, otherwise FALSE
	 */
	public void setLoadComments(boolean loadComments) {
		_loadComments = loadComments;
	}
	
	/**
	 * Sets an equipment type for this search.
	 * @param eqType the equipment type
	 */
	public void setEquipmentType(String eqType) {
		if (!"-".equals(eqType))
			_eqType = eqType;
	}
	
    /**
     * Sets the Arrival Airport object for this search.
     * @param a the Arrival Airport object
     */
    public void setAirportA(Airport a) {
        _airportA = a;
    }

    /**
     * Sets the Departure Airport object for this search.
     * @param a the Departure Airport object
     */
    public void setAirportD(Airport a) {
        _airportD = a;
    }
}