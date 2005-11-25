// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.util;

import java.util.*;

/**
 * A utility class to help with security role filtering.
 * @author Luke
 * @version 1.0
 * @since 1.0 
 */

public class RoleUtils {

    // Singleton constructor
    private RoleUtils() {
    }

    /**
     * Validates wether a user has access to a role-limited resource.
     * @param userRoles a Collection of the user's role names
     * @param rsrcRoles a Collection of the resource's restricted roles
     * @return TRUE if any member of userRoles is contained within rsrcRoles, otherwise FALSE
     */
    public static boolean hasAccess(Collection<String> userRoles, Collection<String> rsrcRoles) {

        // If we have the admin role or the resource is unprotected, allow access
        if (userRoles.contains("Admin") || rsrcRoles.contains("*"))
            return true;

        // Check if we have any of the resource roles
        List<String> tmpRoles = new ArrayList<String>(userRoles);
        tmpRoles.retainAll(rsrcRoles);
        return (tmpRoles.size() > 0);
    }
}