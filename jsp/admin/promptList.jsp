<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title>LLM Prompts</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1">
<content:googleAnalytics />
<content:js name="common" />
<content:newRelic>
<content:cspHeader />
</content:newRelic>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="prompts">
<!-- Table Header Bar-->
<tr class="title caps">
 <td colspan="3" class="left"><span class="nophone"><content:airline />&nbsp;</span>LLM PROMPTS</td>
 <td><el:cmdbutton url="prompt" op="edit" label="NEW PROMPT" /></td>
</tr>
<tr class="title caps">
 <td style="width:20%;">ID</td>
 <td style="width:20%;">DESCRIPTION</td>
 <td style="width:30%;" class="nophone">USER PROMPT</td>
 <td class="nophone">SYSTEM PROMPT</td>
</tr>

<!-- Table Data -->
<c:forEach var="p" items="${viewContext.results}">
<view:row entry="${p}">
 <td class="pri bld"><el:cmd url="prompt" linkID="${p.ID}">${p.ID}</el:cmd></td>
 <td>${p.description}</td>
 <td class="small left top nophone"><fmt:msg value="${p.user}" /></td>
 <td class="small left top nophone"><fmt:msg value="${p.system}" /></td>
</view:row>
</c:forEach>

<!-- Scroll bar -->
<tr class="title">
 <td colspan="4">&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
</body>
</html>
