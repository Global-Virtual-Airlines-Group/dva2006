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
<title><content:airline /> NOTAMs</title>
<content:sysdata var="airlineName" name="airline.name" />
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:rss title="${airlineName} NOTAMs" path="/notams_rss.ws" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" pad="default" space="default" cmd="notams">
<!-- Table Header Bar-->
<tr class="title caps">
 <td width="5%">ID</td>
 <td width="15%">DATE</td>
 <td>SUBJECT</td>
</tr>

<!-- Table NOTAM Data -->
<c:forEach var="notam" items="${viewContext.results}">
<view:row entry="${notam}">
 <td class="priB"><fmt:int value="${notam.ID}" /></td>
 <td class="bld"><fmt:date fmt="d" date="${notam.date}" /></td>
 <td><el:cmd url="notamedit" link="${notam}"><fmt:text value="${notam.subject}" /></el:cmd></td>
</view:row>
<view:row entry="${notam}">
<c:if test="${notam.isHTML}">
 <td colspan="3" class="left">${notam.body}</td>
</c:if>
<c:if test="${!notam.isHTML}">
 <td colspan="3" class="left"><fmt:text value="${notam.body}" /></td>
</c:if>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="3">
<c:if test="${access.canCreateNOTAM}">
<el:cmd url="notamedit">NEW NOTAM</el:cmd>&nbsp;|&nbsp;
</c:if>
<el:cmd url="notams" op="all">ALL NOTAMs</el:cmd>&nbsp;|<view:scrollbar>&nbsp;<view:pgUp />
&nbsp;<view:pgDn /></view:scrollbar>&nbsp;|&nbsp;<el:cmd url="notams">ACTIVE NOTAMs</el:cmd></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
