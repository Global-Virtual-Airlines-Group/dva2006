<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Online Help</title>
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
<view:table className="view" pad="default" space="default" cmd="helplist">
<!-- Table Header Bar-->
<tr class="title caps">
 <td width="20%">ENTRY NAME</td>
 <td width="25%">SUBJECT</td>
 <td>ENTRY TEXT</td>
</tr>

<!-- Table data -->
<c:forEach var="help" items="${viewContext.results}">
<view:row entry="${help}">
 <td><el:cmd url="help" linkID="${help.title}" op="edit" className="pri bld">${help.title}</el:cmd></td>
 <td class="small">${help.subject}</td>
 <td class="small left">${help.body}</td>
</view:row>
</c:forEach>

<!-- Button Bar -->
<tr class="title">
 <td colspan="3"><el:cmdbutton url="msgtemplate" op="new" label="NEW ONLINE HELP ENTRY" /></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
