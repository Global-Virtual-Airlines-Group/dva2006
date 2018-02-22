<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Duplicate Registration Address</title>
<content:css name="main" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="airlineName" name="airline.name" />
<content:sysdata var="addrInterval" name="registration.ip_interval" />

<!-- Main Body Frame -->
<content:region id="main">
<div class="updateHdr">Duplicate Registration Address</div>
<br />
Your IP Address ( <span class="sec bld">${pageContext.request.remoteAddr}</span> <c:if test="${!empty ipInfo}"> - <el:flag countryCode="${ipInfo.country.code}" caption="${ipInfo.location}" /> ${ipInfo.location} </c:if>) has 
been used within the past <fmt:int value="${addrInterval}" /> days to register at the <content:airline /> web site, and the previous applicationis Pending or has already been Approved. You cannot register again from this IP address.<br />
<br />
<span class="pri bld">If you are a former <content:airline /> Pilot who has been marked Retired or Inactive, you do NOT need to register again.</span><br />
<br />
To reactivate your membership at <content:airline />, please <el:cmd url="register" op="dupe" className="sec bld">Click Here</el:cmd>.<br />
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
