<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Login Address Lookup</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.id, 4, 'Host name or IP Address')) return false;

setSubmit();
disableButton('SearchButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="loginaddrs.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">SEARCH USER LOGIN ADDRESSES</td>
</tr>
<tr>
 <td class="label" valign="top">Address / Host Name</td>
 <td class="data"><el:text name="id" idx="*" className="bld req" size="40" max="96" value="${param.id}" />
<div class="small">Use '%' as a wildcard</div></td>
</tr>
<tr>
 <td class="label" valign="top">Network Mask</td>
 <td class="data"><el:text name="mask1" idx="*" size="3" max="3" value="${param.mask1}" />
 <el:text name="mask2" idx="*" size="3" max="3" value="${param.mask2}" />
 <el:text name="mask3" idx="*" size="3" max="3" value="${param.mask3}" />
 <el:text name="mask4" idx="*" size="3" max="3" value="${param.mask4}" />
<div class="small">Providing a network mask allows you to search a particular network. It will also 
cause the provided host name to be translated into an IP address before searching.</div></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SearchButton" type="submit" className="BUTTON" label="SEARCH USER LOGINS" /></td>
</tr>
</el:table>
</el:form>

<c:if test="${!empty addrs}">
<view:table className="view" pad="default" space="default" cmd="loginaddrs">
<tr class="title caps">
 <td colspan="6" class="left">SEARCH RESULTS <fmt:int value="${fn:sizeof(addrs)}" /> RESULTS</td>
</tr>

<!-- Table Header Bar -->
<tr class="title caps">
 <td width="10%">CODE</td>
 <td width="20%">PILOT NAME</td>
 <td width="30%">HOST NAME</td>
 <td width="15%">IP ADDRESS</td>
 <td width="10%">LOGINS</td>
 <td>LAST LOGIN</td>
</tr>

<!-- Table Log Data -->
<c:forEach var="addr" items="${addrs}">
<c:set var="pilot" value="${pilots[addr.ID]}" scope="request" />
<view:row entry="${pilot}">
 <td class="pri bld">${pilot.pilotCode}</td>
 <td><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
 <td>${(addr.remoteAddr == addr.remoteHost) ? 'UNKNOWN' : addr.remoteHost}</td>
 <td>${addr.remoteAddr}</td>
 <td><fmt:int value="${addr.loginCount}" /></td>
 <td><fmt:date fmt="d" default="-" date="${pilot.lastLogin}" /></td>
</view:row>
</c:forEach>
<tr class="title caps">
 <td colspan="6">&nbsp;</td>
</tr>
</view:table>
</c:if>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
