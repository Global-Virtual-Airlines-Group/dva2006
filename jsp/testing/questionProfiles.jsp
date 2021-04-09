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
<title><content:airline /> Examination Questions</title>
<content:css name="main" />
<content:css name="view" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script>
golgotha.local.update = function() { return document.forms[0].submit(); };
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="qprofiles.do" method="post" validate="return true">
<view:table cmd="qprofiles">
<!-- Table Header Bar -->
<tr class="title">
 <td style="width:6%">&nbsp;</td>
 <td style="width:12%">CORRECT / ASKED</td>
 <td style="width:6%">&nbsp;</td>
 <td style="width:7%">&nbsp;</td>
 <td style="width:20%"><c:if test="${access.canCreate}"><el:cmdbutton url="qprofile" op="edit" label="NEW QUESTION" />&nbsp;<el:cmdbutton url="qprofile" op="edit&isRP=true" label="NEW ROUTE PLOT" /></c:if></td>
 <td class="right">SELECT <el:combo name="id" idx="*" size="1" options="${examNames}" value="${param.id}" onChange="void golgotha.local.update()" />
 MIN EXAMS <el:text name="minExams" idx="*" size="3" max="5" value="${minExams}" />
 <el:box name="isAcademy" value="true" checked="${academyOnly}" className="small" label="Academy Only" />&nbsp;<el:button type="submit" label="GO" /></td>
</tr>

<!-- Table Question Data -->
<c:forEach var="q" items="${viewContext.results}">
<view:row entry="${q}">
 <td><el:cmd className="pri bld" url="qprofile" link="${q}">VIEW</el:cmd></td>
 <td><fmt:int value="${q.passCount}" /> / <fmt:int value="${q.total}" /></td>
 <td><c:if test="${q.total > 0}"><fmt:dec value="${q.passCount * 100 / q.total}" fmt="##0.0" />%</c:if><c:if test="${q.total == 0}">-</c:if></td>
 <td>&nbsp;<c:if test="${fn:isMultiChoice(q)}"><el:img src="testing/multiChoice.png" caption="Multiple Choice" /></c:if>
<c:if test="${q.size > 0}"><el:img src="testing/image.png" caption="Image Resource" /></c:if></td>
 <td class="left small" colspan="2"><fmt:text value="${q.question}" /></td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title caps">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
