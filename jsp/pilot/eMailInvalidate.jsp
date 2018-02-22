<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title>E-Mail Address Invalidated</title>
<content:css name="main" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
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
The e-mail address for ${pilot.name} has been marked invalid. The next time that ${pilot.firstName} logs into the <content:airline /> web site, he or she will be prompted for a new, valid e-mail address until the
e-mail address supplied is validated.<br />
<c:if test="${alreadyInvalid}">
<br />
${pilot.firstName}'s e-mail address is already invalid, no additional action has been taken.<br />
</c:if>
<br />
To view this Pilot's profile, <el:cmd url="profile" link="${pilot}" className="sec bld">Click Here</el:cmd>.<br />
</c:if>
<c:if test="${addrUpdate}">
<div class="updateHdr">E-Mail Address Updated</div>
<br />
You have updated your e-mail address to ${addr.address}.<br />
<br />
Membership at <content:airline /> requires that you provide us a validated e-mail address. An e-mail message has been sent to this address with a validation code. When you receive this validation code, please 
<el:cmd url="pilotcenter" className="sec bld">Click Here</el:cmd> to validate your e-mail address.<br />
</c:if>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
