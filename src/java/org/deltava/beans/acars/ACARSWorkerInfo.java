// Copyright 2005 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.Collection;

/**
 * An interface to allow ACARS implementations to return worker thread information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface ACARSWorkerInfo {

	public Collection getWorkers();
}