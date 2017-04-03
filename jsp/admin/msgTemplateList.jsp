<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<html lang="en">
<head>
<title><content:airline /> Message Templates</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:favicon />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="msgtemplates">
<!-- Table Header Bar-->
<tr class="title caps">
 <td style="width:20%">TEMPLATE NAME</td>
 <td class="nophone" style="max-width:5%;">&nbsp;</td>
 <td class="nophone" style="width:30%">MESSAGE SUBJECT</td>
 <td>DESCRIPTION</td>
 <td><c:if test="${access.canCreate}">
<el:cmdbutton url="msgtemplate" op="edit" label="NEW MESSAGE TEMPLATE" /> 
</c:if>&nbsp;</td>
</tr>

<!-- Table data -->
<c:forEach var="template" items="${viewContext.results}">
<view:row entry="${template}">
 <td><el:cmd url="msgtemplate" className="pri bld" linkID="${template.name}" op="edit">${template.name}</el:cmd></td>
 <td class="sec bld nophone">${template.isHTML ? 'HTML' : '&nbsp;'}</td>
 <td class="sec nophone">${template.subject}</td>
 <td colspan="2" class="left">${template.description}</td>
</view:row>
</c:forEach>

<!-- Button Bar -->
<tr class="title">
 <td colspan="5"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
