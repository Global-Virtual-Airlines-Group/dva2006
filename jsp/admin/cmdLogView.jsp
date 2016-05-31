<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Command Log</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="cmdlog.do" method="post" validate="return true">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> WEB SITE COMMAND LOG</td>
</tr>
<tr>
 <td class="label">IP Address / Host</td>
 <td class="data"><el:text name="addr" idx="*" size="48" max="96" value="${param.addr}" /></td>
</tr>
<tr>
 <td class="label">Pilot Name</td>
 <td class="data"><el:text name="pilotName" idx="*" size="32" max="48" value="${param.pilotName}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SearchButton" type="submit" label="SEARCH LOG" /></td>
</tr>
</el:table>
</el:form>
<c:if test="${doSearch}">
<view:table cmd="cmdlog">
<tr class="title caps">
 <td colspan="6" class="left">COMMAND LOG RESULTS</td>
</tr>
<c:if test="${empty viewContext.results}">
<tr>
 <td colspan="6" class="pri bld">No Command Log entries matching your criteria were found.</td>
</tr>
</c:if>
<c:if test="${!empty viewContext.results}">
<tr class="title">
 <td style="width:15%">DATE</td>
 <td style="width:20%">PILOT NAME</td>
 <td style="width:35%">ADDRESS</td>
 <td style="width:10%">COMMAND</td>
 <td style="width:10%">TIME</td>
 <td>DB TIME</td>
</tr>
<c:forEach var="entry" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[entry.pilotID]}" scope="page" />
<view:row entry="${entry}">
 <td class="small"><fmt:date d="MM/dd/yy" t="HH:mm:ss" date="${entry.date}" /></td>
<c:if test="${entry.pilotID != 0}">
 <td><el:cmd url="profile" link="${pilot}" className="pri bld">${pilot.name}</el:cmd></td>
</c:if>
<c:if test="${entry.pilotID == 0}">
 <td class="sec bld">ANONYMOUS</td>
</c:if>
 <td class="small">${entry.remoteHost} (${entry.remoteAddr})</td>
 <td class="pri bld">${entry.name}</td>
 <td><fmt:int value="${entry.time}" /> ms</td>
 <td><fmt:int value="${entry.backEndTime}" /> ms</td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</c:if>
</view:table>
</c:if>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
