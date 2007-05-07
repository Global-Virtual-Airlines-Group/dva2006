<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
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
<script language="JavaScript" type="text/javascript">
function setExamName(combo)
{
var opt = combo.options[combo.selectedIndex];
self.location = 'qprofiles.do?id=' + ((combo.selectedIndex == 0) ? opt.value : opt.text);
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
<el:form action="qprofiles.do" method="get" validate="return false">
<view:table className="view" pad="default" space="default" cmd="qprofiles">
<!-- Table Header Bar -->
<tr class="title">
 <td width="8%">&nbsp;</td>
 <td width="6%">CORRECT</td>
 <td width="6%">ASKED</td>
 <td width="6%">&nbsp;</td>
 <td width="15%" colspan="2" class="left">QUESTION TEXT</td>
 <td width="15%"><c:if test="${access.canEdit}"><el:cmdbutton url="qprofile" op="edit" label="NEW QUESTION" /> </td></c:if>
 <td class="right">SELECT EXAMINATION <el:combo name="eName" size="1" options="${examNames}" value="${param.id}" onChange="void setExamName(this)" /></td>
</tr>

<!-- Table Question Data -->
<c:forEach var="q" items="${viewContext.results}">
<view:row entry="${q}">
 <td><el:cmd className="pri bld" url="qprofile" link="${q}">VIEW</el:cmd></td>
 <td><fmt:int value="${q.correctAnswers}" /></td>
 <td><fmt:int value="${q.totalAnswers}" /></td>
 <td><c:if test="${q.totalAnswers > 0}"><fmt:dec value="${q.correctAnswers * 100 / q.totalAnswers}" fmt="##0.0" />%</c:if>
<c:if test="${q.totalAnswers == 0}">-</c:if></td>
 <td width="8%">&nbsp;<c:if test="${fn:isMultiChoice(q)}"><el:img src="testing/multiChoice.png" caption="Multiple Choice" /></c:if>
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
</body>
</html>
