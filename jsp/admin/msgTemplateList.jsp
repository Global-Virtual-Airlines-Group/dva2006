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
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" cmd="msgtemplates">
<!-- Table Header Bar-->
<tr class="title caps">
 <td style="width:20%">TEMPLATE NAME</td>
 <td style="width:5%">&nbsp;</td>
 <td style="width:30%">MESSAGE SUBJECT</td>
 <td style="width:30%">DESCRIPTION</td>
 <td><c:if test="${access.canCreate}">
<el:cmdbutton url="msgtemplate" op="edit" label="NEW MESSAGE TEMPLATE" /> 
</c:if>&nbsp;</td>
</tr>

<!-- Table data -->
<c:forEach var="template" items="${templates}">
<view:row entry="${template}">
 <td><el:cmd url="msgtemplate" className="pri bld" linkID="${template.name}" op="edit">${template.name}</el:cmd></td>
 <td class="sec bld">${template.isHTML ? 'HTML' : '&nbsp;'}</td>
 <td class="sec">${template.subject}</td>
 <td colspan="2" class="left">${template.description}</td>
</view:row>
</c:forEach>

<!-- Button Bar -->
<tr class="title">
 <td colspan="5">&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
