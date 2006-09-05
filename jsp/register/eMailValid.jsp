<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>E-Mail Address Validated</title>
<content:css name="main" browserSpecific="true" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<c:if test="${!isCancel}">
<div class="updateHdr">E-Mail Address Validated</div>
<br />
Your e-mail address (${person.email}) has been successfuly verified. This address will be used 
for all <content:airline />-related correspondence in the future.<br />
<c:if test="${isApplicant}">
<br />
Thank you for verifying your e-mail address. This is an important step in the registration 
process here at <content:airline />.<br />
</c:if>
</c:if>
<c:if test="${isCancel}">
<div class="updateHdr">E-Mail Address Validation Canceled</div>
<br />
<c:if test="${forceAccept}">
E-Mail Address validation for ${person.name} has been overriden, and the new e-mail address of ${addr.address} 
will be used for all <content:airline />-related correspondence in the future.<br />
</c:if>
<c:if test="${!forceAccept}">
E-Mail Address validation for ${person.name} has been canceled. The original e-mail address of ${pilot.email} 
will continue to be used for all <content:airline />-related correspondence in the future.<br />
</c:if>
</c:if>
<c:if test="${!empty pilot}">
<br />
To return to the <content:airline /> Pilot Center, <el:cmd url="pilotcenter" className="sec bld">click here</el:cmd>.<br />
</c:if>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
