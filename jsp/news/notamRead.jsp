<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> NOTAM</title>
<content:css name="main" browserSpecific="true" />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<%@include file="/jsp/main/header.jsp" %> 
<%@include file="/jsp/main/sideMenu.jsp" %>

<!-- Main Body Frame -->
<div id="main">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2"><content:airline /> NOTICE TO AIRMEN</td>
</tr>
<tr>
 <td class="label">Notice Title</td>
 <td class="data pri bld">${entry.subject}</td>
</tr>
<tr>
 <td class="label">Notice Date</td>
 <td class="data"><fmt:date fmt="d" date="${entry.date}" /></td>
</tr>
<c:if test="${entry.active}">
<tr>
 <td class="label">&nbsp;</td>
 <td class="data ter bld caps">Notice to Airmen is currently In Effect</td>
</tr>
</c:if>
<tr>
 <td class="label" valign="top">Entry Text</td>
 <td class="data"><fmt:text value="${entry.body}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td>&nbsp;
<c:if test="${access.canDelete}">
<el:cmdbutton url="newsdelete" op="NOTAM" linkID="0x${entry.ID}" label="DELETE NOTAM" />
</c:if>
 </td>
</tr>
</el:table>
<content:copyright />
</div>
</body>
</html>
