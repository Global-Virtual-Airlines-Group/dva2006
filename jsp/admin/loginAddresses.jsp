<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
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
 <td class="label top">Address / Host Name</td>
 <td class="data"><el:text name="id" idx="*" className="bld req" size="40" max="96" value="${param.id}" />
<span class="small">Use '%' as a wildcard</span></td>
</tr>
<c:if test="${!empty addrInfo}">
<tr>
 <td class="label">IP Address Info</td>
 <td class="data">${addrInfo.block} <el:flag countryCode="${addrInfo.countryCode}" caption="${addrInfo.country}" /> ${addrInfo.location}</td>
</tr>
</c:if>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="searchNet" value="true" idx="*" checked="${param.searchNet}" label="Search entire Network block" /></td>
</tr>
<content:hasmsg>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data error bld"><content:sysmsg /></td>
</tr>
</content:hasmsg>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SearchButton" type="submit" className="BUTTON" label="SEARCH USER LOGINS" /></td>
</tr>
</el:table>
</el:form>

<c:if test="${doSearch}">
<view:table className="view" pad="default" space="default" cmd="loginaddrs">
<tr class="title caps">
 <td colspan="6" class="left">SEARCH RESULTS<c:if test="${!empty addrs}"> - <fmt:int value="${fn:sizeof(addrs)}" /> RESULTS</c:if></td>
</tr>
<c:if test="${empty addrs}">
<tr>
 <td colspan="6" class="pri bld caps">NO MATCHING <content:airline /> PILOTS WERE FOUND.</td>
</tr>
</c:if>
<c:if test="${!empty addrs}">
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
<c:set var="pilot" value="${pilots[addr.ID]}" scope="page" />
<view:row entry="${pilot}">
 <td class="pri bld">${pilot.pilotCode}</td>
 <td><el:cmd url="profile" link="${pilot}">${pilot.name}</el:cmd></td>
 <td>${(addr.remoteAddr == addr.remoteHost) ? 'UNKNOWN' : addr.remoteHost}</td>
 <td>${addr.remoteAddr}</td>
 <td><fmt:int value="${addr.loginCount}" /></td>
 <td><fmt:date fmt="d" default="-" date="${pilot.lastLogin}" /></td>
</view:row>
</c:forEach>
<tr class="title">
 <td colspan="6"><view:legend width="110" labels="Active,Inactive,Retired,On Leave,Suspended" classes=" ,opt2,opt3,warn,error" /></td>
</tr>
</c:if>
</view:table>
</c:if>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
