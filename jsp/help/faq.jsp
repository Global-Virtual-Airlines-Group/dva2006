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
<title><content:airline /> Frequently Asked Questions</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<view:table className="view" space="default" pad="default" cmd="faq">
<tr class="title">
 <td colspan="2" class="left caps"><content:airline /> FREQUENTLY ASKED QUESTIONS</td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td width="30%">QUESTION</td>
 <td>&nbsp;</td>
</tr>

<!-- Table View data -->
<c:forEach var="issue" items="${viewContext.results}">
<c:set var="comment" value="${comments[issue.ID]}" scope="request" />
<td>
 <td class="pri bld">${issue.subject}</td>
 <td class="small left"><fmt:msg value="${comment.body}" /></td>
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
</body>
</html>
