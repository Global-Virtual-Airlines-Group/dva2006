<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Examination Questions</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script type="text/javascript">
function update()
{
var f = document.forms[0];
f.submit();
return true;
}
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
<view:table className="view" cmd="qprofiles">
<!-- Table Header Bar -->
<tr class="title">
 <td width="7%">&nbsp;</td>
 <td width="6%">CORRECT</td>
 <td width="6%">ASKED</td>
 <td width="6%">&nbsp;</td>
 <td width="15%" colspan="2" class="left">QUESTION TEXT</td>
 <td width="20%"><c:if test="${access.canCreate}"><el:cmdbutton url="qprofile" op="edit" label="NEW QUESTION" />
 <el:cmdbutton url="qprofile" op="edit&isRP=true" label="NEW ROUTE PLOT" /></c:if></td>
 <td class="right">SELECT <el:combo name="id" idx="*" size="1" options="${examNames}" value="${param.id}" onChange="void update()" />
 MIN EXAMS <el:text name="minExams" idx="*" size="3" max="5" value="${param.minExams}" /> <el:button type="submit" label="GO" /></td>
</tr>

<!-- Table Question Data -->
<c:forEach var="q" items="${viewContext.results}">
<view:row entry="${q}">
 <td><el:cmd className="pri bld" url="qprofile" link="${q}">VIEW</el:cmd></td>
 <td><fmt:int value="${q.correctAnswers}" /></td>
 <td><fmt:int value="${q.totalAnswers}" /></td>
 <td><c:if test="${q.totalAnswers > 0}"><fmt:dec value="${q.correctAnswers * 100 / q.totalAnswers}" fmt="##0.0" />%</c:if>
<c:if test="${q.totalAnswers == 0}">-</c:if></td>
 <td width="7%">&nbsp;<c:if test="${fn:isMultiChoice(q)}"><el:img src="testing/multiChoice.png" caption="Multiple Choice" /></c:if>
<c:if test="${q.size > 0}"><el:img src="testing/image.png" caption="Image Resource" /></c:if></td>
 <td class="left small" colspan="3">${q.question}</td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title caps">
 <td colspan="8"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
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
