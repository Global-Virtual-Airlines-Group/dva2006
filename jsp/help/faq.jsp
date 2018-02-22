<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Frequently Asked Questions</title>
<content:expire expires="3600" />
<content:css name="main" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/help/header.jspf" %> 
<%@ include file="/jsp/help/sideMenu.jspf" %>
<content:attr attr="isHR" value="true" roles="HR" />

<!-- Main Body Frame -->
<content:region id="main">
<view:table cmd="faq">
<tr class="title">
 <td colspan="2" class="left caps"><content:airline /> FREQUENTLY ASKED QUESTIONS</td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:30%">QUESTION</td>
 <td>&nbsp;</td>
</tr>

<!-- Table View data -->
<c:forEach var="issue" items="${viewContext.results}">
<c:set var="comment" value="${fn:first(issue.comments)}" scope="page" />
<tr>
<c:if test="${isHR}">
 <td><el:cmd url="hdissue" link="${issue}" className="pri bld"><fmt:text value="${issue.subject}" /></el:cmd></td>
</c:if>
<c:if test="${!isHR}">
 <td class="pri bld">${issue.subject}</td>
</c:if>
 <td class="left">${issue.body}</td>
</tr>
<tr>
 <td colspan="2" class="left"><fmt:msg value="${comment.body}" /></td>
</tr>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title caps">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn />&nbsp;</view:scrollbar>
<view:legend width="95" labels="Open,Resolved" classes=" ,opt1" /></td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
