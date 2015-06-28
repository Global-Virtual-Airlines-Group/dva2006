// Copyright 2012, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.OutputStream;

/**
 * A Data Access Object to write to streams.
 * @author Luke
 * @version 6.0
 * @since 4.1
 */

abstract class WriteableDAO extends DAO {
	
	protected final OutputStream _os;

	/**
	 * Initializes the Data Access Object.
	 * @param os the OutputStream to write to
	 */
	protected WriteableDAO(OutputStream os) {
		super(null);
		_os = os;
	}
}