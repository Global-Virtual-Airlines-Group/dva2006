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
<title><content:airline /> Check Ride Scripts</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<style>
span.el {
  display: block; /* Fallback for non-webkit */
  display: -webkit-box;
  max-width: 95%;
  height: 11*1.4*8; /* Fallback for non-webkit */
  margin: 0 auto;
  line-height: 1.4;
  -webkit-line-clamp: 8;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<c:set var="access" value="${accessMap['NEW']}" scope="page" />
<content:sysdata var="currencyEnabled" name="testing.currency.enabled" />

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="crscripts">
<!-- Table Header Bar -->
<tr class="title">
 <td style="width:10%">AIRCRAFT TYPE</td>
 <td style="width:10%">EQUIPMENT PROGRAM</td>
 <td style="width:65%" class="left nophone">DESCRIPTION</td>
 <td> <c:if test="${access.canCreate}"><el:cmd url="crscript" op="edit">NEW SCRIPT</el:cmd></c:if></td>
</tr>

<!-- Table Script Data -->
<c:forEach var="sc" items="${viewContext.results}">
<view:row entry="${sc}">
<c:set var="access" value="${accessMap[sc.equipmentType]}" scope="page" />
<td>
<c:if test="${access.canEdit}"><el:cmd url="crscript" linkID="${sc.auditID}" op="edit">${sc.equipmentType}</el:cmd></c:if>
<c:if test="${!access.canEdit}"><span class="pri bld">${sc.equipmentType}</span></c:if>
<c:if test="${sc.isCurrency}"><br />
<span class="ter bld small">CURRENCY</span></c:if></td>
 <td class="sec bld">${sc.program}</td>
 <td class="small left nophone" colspan="2"><span class="el"><fmt:msg value="${sc.description}" bbCode="true" /></span></td>
</view:row>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title">
 <td colspan="4"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
