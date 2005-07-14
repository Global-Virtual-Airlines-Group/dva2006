<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Pilot Leave of Absence</title>
<content:css name="main" browserSpecific="true" />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>
<content:sysdata var="interval" name="users.inactive_leave_days" />

<!-- Main Body Frame -->
<div id="main">
<div class="updateHdr"><content:airline /> Pilot Leave of Absence</div>
<br />
You have been placed on a Leave of Absence for up to <fmt:dec value="${interval}" /> days. To 
return to Active status, you just need to log into the web site again.<br />
<br />
</div>
</body>
</html>
