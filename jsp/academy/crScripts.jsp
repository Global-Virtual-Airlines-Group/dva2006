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
<title><content:airline /> Flight Academy Check Ride Scripts</title>
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
<%@ include file="/jsp/academy/header.jspf" %> 
<%@ include file="/jsp/academy/sideMenu.jspf" %>
<c:set var="ac" value="${access['NEW']}" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="crscripts">
<!-- Table Header Bar -->
<tr class="title">
 <td style="width:15%">CERTIFICATION</td>
 <td style="width:5%">#</td>
 <td style="width:60%" class="left">CHECK RIDE DESCRIPTION</td>
 <td> <c:if test="${ac.canCreate}"><el:cmd url="arscript" op="edit">NEW SCRIPT</el:cmd></c:if></td>
</tr>

<!-- Table Script Data -->
<c:forEach var="sc" items="${scripts}">
<view:row entry="${sc}">
<c:set var="ac" value="${access[sc.certificationName]}" scope="page" />
<c:if test="${ac.canEdit}">
 <td><el:cmd url="arscript" linkID="${sc.certificationName}-${sc.index}" className="pri bld" op="edit">${sc.certificationName}</el:cmd></td>
</c:if>
<c:if test="${!ac.canEdit}">
 <td class="pri bld">${sc.certificationName}</td>
</c:if>
 <td class="sec bld"><fmt:int value="${sc.index}" /></td>
 <td class="small left" colspan="2"><span class="el"><fmt:msg value="${sc.description}" bbCode="true" /></span></td>
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
