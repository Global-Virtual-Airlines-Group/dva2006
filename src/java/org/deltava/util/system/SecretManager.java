// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.system;

import java.io.IOException;
import java.util.Collection;

/**
 * An interface for secret/credential managers.
 * @author Luke
 * @version 10.5
 * @since 10.5
 */

public interface SecretManager {

	/**
	 * Retrieves a secret.
	 * @param name the secret name
	 * @return the secret value, or null if not found
	 */
	public String get(String name);
	
	/**
	 * Returns the names of all the secrets.
	 * @return a Collection of secret names
	 */
	public Collection<String> getKeys();
	
	/**
	 * Returns the number of loaded secrets.
	 * @return the number of secrets
	 */
	public int size();
	
	/**
	 * Optional initialization method
	 * @throws IOException if an erorr occurs
	 */
	public void load() throws IOException;
}