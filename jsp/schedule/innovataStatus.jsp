<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Innovata Schedule Import Status</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body onload="void initLinks()">
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">INNOVATA FLIGHT SCHEDULE DOWNLOAD STATUS</td>
</tr>
<c:if test="${!empty msgs}">
<tr>
 <td class="label top">Import Messages</td>
 <td class="data"><c:forEach var="msg" items="${msgs}">
<fmt:text value="${msg}" /><br /></c:forEach></td>
</tr>
</c:if>
<c:if test="${!empty eqTypes}">
<tr>
 <td class="label top">Invalid Equipment</td>
 <td class="data"><fmt:list value="${eqTypes}" delim=", " /></td>
</tr>
</c:if>
<c:if test="${!empty airports}">
<tr>
 <td class="label top">Invalid Airports</td>
 <td class="data"><c:forEach var="code" items="${airports}">
Invalid Airport Code - <el:cmd url="airport" linkID="${code}" op="edit" target="_new" className="bld">${code}</el:cmd><br />
</c:forEach></td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:cmdbutton ID="DownloadButton" url="ivimport" label="DOWNLOAD FLIGHT SCHEDULE" /></td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
