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
<title><content:airline /> Web Resources</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function sort()
{
document.forms[0].submit();
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="hrEmail" name="airline.mail.hr" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="resources.do" method="post" validate="return false">
<view:table className="view" pad="default" space="default" cmd="resources">
<tr class="title caps">
 <td class="left" colspan="2"><content:airline /> WEB RESOURCES</td>
<c:if test="${access.canCreate}">
 <td colspan="2"><el:cmdbutton url="resource" op="edit" label="NEW RESOURCE" /></td>
</c:if>
<c:if test="${!access.canCreate}">
 <td colspan="2">&nbsp;</td>
</c:if>
 <td class="right" colspan="2">SORT BY <el:combo name="sortType" idx="*" size="1" options="${sortOptions}" value="${viewContext.sortType}" onChange="void sort()" /></td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td width="5%">&nbsp;</td>
 <td width="30%">RESOURCE URL</td>
 <td width="15%">AUTHOR</td>
 <td width="10%">CREATED ON</td>
 <td width="5%">HITS</td>
 <td class="left">DESCRIPTION</td>
</tr>

<!-- Table Data Section -->
<c:forEach var="resource" items="${viewContext.results}">
<c:set var="rAccess" value="${accessMap[resource.ID]}" scope="request" />
<c:set var="author" value="${pilots[resource.authorID]}" scope="request" />
<view:row entry="${resource}">
<c:if test="${rAccess.canEdit}">
 <td><el:cmdbutton url="resource" linkID="0x${resource.ID}" op="edit" label="EDIT" /></td>
</c:if>
 <td colspan="${rAccess.canEdit ? '1' : '2'}"><el:cmd url="gotoresource" linkID="0x${resource.ID}" className="pri bld small">${resource.URL}</el:cmd></td>
 <td><el:cmd url="profile" linkID="0x${author.ID}" className="bld">${author.name}</el:cmd></td>
 <td class="sec"><fmt:date date="${resource.createdOn}" fmt="d" /></td>
 <td class="small"><fmt:int value="${resource.hits}" /></td>
 <td class="small left"><fmt:text value="${resource.description}" /></td>
</view:row>
</c:forEach>

<!-- Disclaimer bar -->
<tr>
 <td colspan="6">All Web Resources listed above are links to external Web Sites. No guarantees are made
 by <content:airline /> regarding the fitness or appropriateness of content contained within these external
 sites. To report inappropriate content, please contact <el:link url="mailto:${hrEmail}">${hrEmail}</el:link>.</td>
</tr>

<!-- Scroll Bar row -->
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
