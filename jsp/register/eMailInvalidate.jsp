<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>E-Mail Address Invalidated</title>
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
<c:if test="${!addrUpdate}">
<div class="updateHdr">E-Mail Address Invalidated</div>
<br />
The e-mail address for ${pilot.name} has been marked invalid. The next time that ${pilot.firstName} logs into 
the <content:airline /> web site, he or she will be prompted for a new, valid e-mail address until the
e-mail address supplied is validated.<br />
<c:if test="${alreadyInvalid}">
<br />
${pilot.firstName}'s e-mail address is already invalid, no additional action has been taken.<br />
</c:if>
<br />
To view this Pilot's profile, <el:cmd url="profile" link="${pilot}" className="sec bld">click here</el:cmd>.<br />
</c:if>
<c:if test="${addrUpdate}">
<div class="updateHdr">E-Mail Address Updated</div>
<br />
You have updated your e-mail address to ${pilot.email}. An e-mail message has been sent to this address with 
a validation code. When you receive this validation code, please <el:cmd url="pilotcenter" className="sec bld">click here</el:cmd> 
to provide the validation code.<br />
</c:if>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
