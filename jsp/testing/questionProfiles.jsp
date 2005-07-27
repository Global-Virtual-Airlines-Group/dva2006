<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Examination Questions</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:css name="form" />
<content:js name="common" />
<script language="JavaScript" type="text/javscript">
function setExamName(combo)
{
var examName = combo.options[combo.selectedIndex].value;
self.location = 'qprofiles.do?id=' + examName;
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<el:form action="qprofiles.do" method="get" validate="return false">
<view:table className="view" pad="default" space="default" cmd="qprofiles">
<!-- Table Header Bar -->
<tr class="title">
 <td width="10%">&nbsp;</td>
 <td width="5%">CORRECT</td>
 <td width="5%">ASKED</td>
 <td width="5%">PERCENT</td>
 <td width="40%">QUESTION TEXT</td>
 <td class="right">SELECT EXAM <el:combo name="eName" size="1" options="${examNames}" value="${param.id}" onChange="void setExamName(this)" /></td>
</tr>

<!-- Table Question Data -->
<c:forEach var="q" items="${viewContext.results}">
<view:row entry="${q}">
 <td><el:cmdbutton url="qprofile" linkID="0x${q.ID}" label="VIEW" /></td>
 <td><fmt:int value="${q.correctAnswers}" /></td>
 <td><fmt:int value="${q.totalAnswers}" /></td>
 <td><fmt:dec value="${q.correctAnswers * 100 / q.totalAnswers}" fmt="##0.0" /></td>
 <td class="left small" colspan="2">${q.question}</td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title caps">
 <td colspan="6"><view:pgUp />&nbsp;<view:pgDn /></td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</div>
</body>
</html>
