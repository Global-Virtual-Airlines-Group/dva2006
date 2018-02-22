<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Pilot Leave of Absence</title>
<content:css name="main" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:css name="form" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="interval" name="users.inactive_leave_days" />

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr"><content:airline /> Pilot Leave of Absence</div>
<br />
You have been placed on a Leave of Absence for up to <fmt:int value="${interval}" /> days. To return to Active status, please log into the <content:airline /> web site again prior to the expiration of your Leave of Absence, which will 
occur on <fmt:date fmt="d" date="${loaExpires}" />.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
