<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<html lang="en">
<head>
<title>Virtual Airline Profiles</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="ainfolist">
<!-- Table Header Bar-->
<tr class="title caps">
 <td style="width:25%">AIRLINE NAME</td>
 <td>CODE</td>
 <td class="nophone">DATABASE</td>
 <td>DOMAIN NAME</td>
 <td class="nophone">&nbsp;</td>
</tr>

<!-- Table Data -->
<c:forEach var="ai" items="${apps}">
<c:set var="ac" value="${access[ai.code]}" scope="page" />
<view:row entry="${ai}">
 <td class="pri bld"><el:cmd url="ainfo" linkID="${ai.code}" op="${ac.canEdit ? 'edit' : 'read'}">${ai.name}</el:cmd></td>
 <td class="sec bld">${ai.code}</td>
 <td class="bld nophone">${ai.DB}</td>
 <td>${ai.domain}</td>
 <td class="nophpone sec small"><c:if test="${ai.canTransfer}">Airline allows inbound Pilot transfers</c:if>
<c:if test="${ai.canTransfer && ai.historicRestricted}"><br /></c:if>
<c:if test="${ai.historicRestricted}">Historic Routes require Historic Aircraft</c:if>
<c:if test="${!ai.canTransfer && !ai.historicRestricted}">-</c:if></td>
</view:row>
</c:forEach>

<!-- Scroll bar -->
<tr class="title">
 <td colspan="5">&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
