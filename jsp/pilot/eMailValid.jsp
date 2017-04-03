<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<html lang="en">
<head>
<title>E-Mail Address Validated</title>
<content:css name="main" />
<content:pics />
<content:favicon />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">E-Mail Address Validated</div>
<br />
Your e-mail address (${person.email}) has been successfuly verified<c:if test="${fbValidate}"> via Facebook</c:if>. This 
address will be used for all <content:airline />-related correspondence in the future.<br />
<br />
To return to the <content:airline /> Pilot Center, <el:cmd url="pilotcenter" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
