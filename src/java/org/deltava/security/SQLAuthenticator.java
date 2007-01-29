// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.security;

import java.sql.Connection;

/**
 * This interface marks a {@link Authenticator} that uses a JDBC Connection to perform operations. Since some
 * operations may need to be performed as part of a single database transaction, this allows us to pass an existing
 * JDBC Connection to the authenticator which is in the middle of a transaction. Otherwise, if we use a new JDBC
 * connection, we may encounter a deadlock between the two transactions.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface SQLAuthenticator extends Authenticator {

	/**
	 * Provides the JDBC connection for this Authenticator to use.
	 * @param c the Connection to use
	 */
	public void setConnection(Connection c);
	
	/**
	 * Clears the explicit JDBC connection for an Authenticator to use, reverting to default behavior.
	 */
	public void clearConnection();
}