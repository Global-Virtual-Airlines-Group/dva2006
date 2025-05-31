<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<html lang="en">
<title><content:airline /> Partners</title>
<content:css name="main" />
<content:css name="view" />
<content:googleAnalytics />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:cspHeader />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="partners">
<tr class="title">
 <td style="width:35%">NAME</td>
 <td style="width:10%"><c:if test="${access.canCreate}"><el:cmdbutton url="partner" op="edit" label="NEW PARTNER" /></c:if>&nbsp;</td>
 <td class="left">DESCRIPTION</td>
</tr>

<!-- Table Partner Data -->
<c:forEach var="partner" items="${viewContext.results}">
<c:set var="ac" value="${accessMap[partner.ID]}" scope="page" />
<tr>
 <td><c:if test="${partner.hasImage}"><a href="/partner.ws?id=${partner.hexID}" rel="nofollow" target="_new"><el:dbimg img="${partner}" caption="${partner.name}" /></a><br /></c:if>
<c:if test="${ac.canEdit}"><el:cmd url="partner" link="${partner}" op="edit" className="sec bld">${partner.name}</el:cmd></c:if>
<c:if test="${!ac.canEdit}"><a href="/partner.ws?id=${partner.hexID}" rel="nofollow" class="pri bld">${partner.name}</a></c:if></td>
 <td colspan="2" class="left">${partner.description}</td>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="3"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
