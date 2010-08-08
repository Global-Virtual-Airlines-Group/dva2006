<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Help Desk Response Templates</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/help/header.jspf" %> 
<%@ include file="/jsp/help/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" pad="default" space="default" cmd="rsptemplates">
<!-- Table Header Bar-->
<tr class="title caps">
 <td width="25%">TEMPLATE NAME</td>
 <td width="15%"><el:cmdbutton url="rsptemplate" op="edit" label="NEW RESPONSE TEMPLATE" /></td>
 <td>RESPONSE BODY</td>
</tr>

<!-- Table data -->
<c:forEach var="template" items="${viewContext.results}">
<tr>
 <td><el:cmd url="rsptemplate" className="pri bld" linkID="${template.title}" op="edit">${template.title}</el:cmd></td>
 <td colspan="2" class="small left"><fmt:text value="${template.body}" /></td>
</tr>
</c:forEach>

<!-- Button Bar -->
<tr class="title">
 <td colspan="3">&nbsp;<c:if test="${access.canUpdateTemplate}"><el:cmdbutton url="rsptemplate" op="edit" label="NEW RESPONSE TEMPLATE" /></c:if></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
 