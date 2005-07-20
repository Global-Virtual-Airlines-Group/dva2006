// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.Collection;

/**
 * An interface to allow ACARS implementations to return Connection Pool diagnostics.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface ACARSAdminInfo {

   public Collection getPoolInfo();
}