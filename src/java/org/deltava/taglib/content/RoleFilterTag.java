// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.taglib.content;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * A JSP tag to filter body content based on the user's membership in a particular role.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class RoleFilterTag extends TagSupport {
    
    private final Collection<String> _roles = new HashSet<String>();

    /**
     * Filters the body content by checking for the user attribute in the request, then retrieves the
     * list of roles from this object.
     * @return SKIP_BODY if role not found, otherwise EVAL_BODY_INCLUDE
     */
    public int doStartTag() {
        
        // Get the request
        HttpServletRequest hreq = (HttpServletRequest) pageContext.getRequest();
      
        // Check if the user has any of the roles listed in our role section
        for (Iterator<String> i = _roles.iterator(); i.hasNext(); ) {
            String role = i.next();
            if (role.charAt(0) == '!') {
                if (!hreq.isUserInRole(role.substring(1)))
                    return EVAL_BODY_INCLUDE;
            } else {
                if (hreq.isUserInRole(role))
                    return EVAL_BODY_INCLUDE;
            }
        }
        
        // If we got this far, we do NOT want to include the body
        return SKIP_BODY;
    }
    
    /**
     * Closes the JSP and releases state.
     * @return EVAL_PAGE always
     */
    public int doEndTag() {
    	release();
    	return EVAL_PAGE;
    }
    
    /**
     * Sets the role(s) a user must belong to in order to view the body of this tag. Use * (asterisk) for all roles,
     * and a role name prefaced by an ! (exclamation mark) for the lack of role memebrship.
     * @param roles a comma-delimited list of authorized role names
     */
    public void setRoles(String roles) {
        StringTokenizer tkns = new StringTokenizer(roles, ",");
        while (tkns.hasMoreTokens())
            _roles.add(tkns.nextToken());
    }
    
    /**
     * Clears state by reseting the role list.
     */
    public void release() {
        _roles.clear();
        super.release();
    }
}