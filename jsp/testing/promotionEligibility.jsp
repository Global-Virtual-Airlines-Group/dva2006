<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title>Promotion Eligibility - ${pilot.name}</title>
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
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="view">
<tr class="title caps">
 <td colspan="3" class="left"><content:airline /> PROMOTION ELIGIBILITY FOR ${pilot.name} <c:if test="${!empty pilot.pilotCode}"> (${pilot.pilotCode})</c:if></td>
</tr>
<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:15%">EQUIPMENT PROGRAM</td>
 <td style="width:5%">STAGE</td>
 <td>PROMOTION ELIGIBILITY</td>
</tr>

<!-- Table Data -->
<c:forEach var="eqType" items="${fn:keys(eqData)}">
<c:set var="msg" value="${eqData[eqType]}" scope="page" />
<view:row entry="${msg}">
 <td class="pri bld">${eqType.name}</td>
 <td class="sec bld"><fmt:int value="${eqType.stage}" /></td>
 <td class="left"><fmt:text value="${msg}" /></td>
</view:row>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title">
 <td colspan="3">&nbsp;</td>
</tr> 
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
