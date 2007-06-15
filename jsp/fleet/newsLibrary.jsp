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
<title><content:airline /> Newsletters</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function setCategory(combo)
{
var catName = combo.options[combo.selectedIndex].text;
self.location = '/newsletters.do?category=' + escape(catName);
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="cats" name="airline.newsletters.categories" />
<content:filter roles="Fleet"><c:set var="isFleetMgr" value="${true}" scope="request" /></content:filter>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="newsletters" method="get" validate="return false">
<view:table className="view" pad="default" space="default" cmd="newslibrary">
<!-- Table Header Bar -->
<tr class="title">
 <td width="30%">TITLE</td>
 <td width="5%">&nbsp;</td>
 <td width="10%">SIZE</td>
<c:choose>
<c:when test="${access.canCreate}">
 <td width="10%"><el:cmdbutton url="newsletter" op="edit" label="NEW NEWSLETTER" /></td>
</c:when>
<c:otherwise>
 <td width="10%">&nbsp;</td>
</c:otherwise>
</c:choose>
 <td width="15%" class="left">DESCRIPTION</td>
 <td class="right">CATEGORY <el:combo name="category" idx="*" size="1" options="${cats}" firstEntry="ALL" value="${catName}" onChange="void setCategory(this)" /></td>
</tr>

<!-- Table Data Section -->
<c:forEach var="doc" items="${docs}">
<tr>
<c:if test="${isFleetMgr}">
 <td class="pri bld"><el:cmd url="newsletter" linkID="${doc.fileName}" op="edit">${doc.name}</el:cmd></td>
</c:if>
<c:if test="${!isFleetMgr}">
 <td class="pri bld"><el:link url="/newsletter/${doc.fileName}">${doc.name}</el:link></td>
</c:if>
 <td><el:link url="/newsletter/${doc.fileName}"><el:img src="library/adobe.png" caption="Download PDF manual" x="32" y="32" border="0" /></el:link></td>
 <td class="sec bld"><fmt:int value="${doc.size}" /></td>
 <td class="small left" colspan="3"><fmt:text value="${doc.description}" /></td>
</tr>
</c:forEach>

<!-- Download Acrobat -->
<tr valign="middle">
 <td><a href="http://www.adobe.com/products/acrobat/readstep2.html"><el:img src="library/getacro.png" border="0" caption="Download Adobe Acrobat Reader" /></a></td>
 <td colspan="5">All <content:airline /> newsletters require <span class="pri bld">Adobe Acrobat Reader 5</span> or
 newer in order to be viewed. If you are having difficulties viewing our newsletters, please click on the link to
 the left to download the latest version of Adobe Acrobat Reader.<br />
This is a free download.</td>
</tr>

<!-- Scroll Bar row -->
<tr class="title">
 <td colspan="6">&nbsp;</td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
