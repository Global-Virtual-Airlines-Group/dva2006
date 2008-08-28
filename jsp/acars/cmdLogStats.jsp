<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> ACARS Server Command Logs</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<content:js name="swfobject" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<%@ include file="/jsp/admin/diag/acarsInfo.jspf" %>
<c:if test="${!empty acarsCmdStats}">
<!-- ACARS Server Command Statistics -->
<el:table className="view" pad="default" space="default">
<tr class="title">
 <td class="left caps" colspan="6">ACARS SERVER STATISTICS</td>
</tr>

<!-- Command Statistics Header -->
<tr class="title">
 <td width="30%">COMMAND NAME</td>
 <td width="10%">INVOCATIONS</td>
 <td width="15%">AVERAGE TIME</td>
 <td width="15%">TOTAL TIME</td>
 <td width="15%">MAXIMUM TIME</td>
 <td>MINIMUM TIME</td>
</tr>

<!-- Command Statistics Data -->
<c:forEach var="cmdStat" items="${acarsCmdStats}">
<tr>
 <td class="pri bld">${cmdStat.name}</td>
 <td><fmt:int value="${cmdStat.count}" /></td>
 <td class="bld"><fmt:int value="${cmdStat.totalTime / cmdStat.count}" /> ms</td>
 <td><fmt:int value="${cmdStat.totalTime}" /> ms</td>
 <td><fmt:int value="${cmdStat.maxTime}" /> ms</td>
 <td><fmt:int value="${cmdStat.minTime}" /> ms</td>
</tr>
</c:forEach>
</el:table>
</c:if>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
