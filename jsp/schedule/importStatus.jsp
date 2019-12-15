<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Raw Schedule Import Status</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><span class="nophone"><content:airline />&nbsp;</span>RAW SCHEDULE IMPORT STATUS</td>
</tr>
<c:forEach var="st" items="${importStatus}">
<c:set var="stats" value="${sourceStats[st.source]}" scope="page" />
<tr class="title caps">
 <td colspan="2">${st.source.description} - <span class="und" onclick="javascript:void golgotha.util.toggleExpand(this, 'import-${st.source}')">COLLAPSE</span></td>
</tr>
<tr class="import-${st.source}">
 <td class="label">Status</td>
 <td class="data">Imported on <span class="sec bld"><fmt:date date="${st.importDate}"  t="HH:mm" /></span> (<span class="pri bld"><fmt:int value="${stats.legs}" /></span> entries)</td>
</tr>
<c:if test="${!empty st.errorMessages}">
<tr class="import-${st.source}">
 <td class="label top">Import Messages</td>
 <td class="data"><c:forEach var="msg" items="${st.errorMessages}">
<fmt:text value="${msg}" /><br /></c:forEach></td>
</tr>
</c:if>
<c:if test="${!empty st.invalidEquipment}">
<tr class="import-${st.source}">
 <td class="label top">Invalid Equipment</td>
 <td class="data"><fmt:list value="${st.invalidEquipment}" delim=", " /></td>
</tr>
</c:if>
<c:if test="${!empty st.invalidAirlines}">
<tr class="import-${st.source}">
 <td class="label top">Invalid Airlines</td>
 <td class="data"><c:forEach var="code" items="${st.invalidAirlines}">
Invalid Airline Code - <el:cmd url="airline" linkID="${code}" op="edit" target="_new" className="bld">${code}</el:cmd><br /> 
</c:forEach></td>
</tr>
</c:if>
<c:if test="${!empty st.invalidAirports}">
<tr class="import-${st.source}">
 <td class="label top">Invalid Airports</td>
 <td class="data"><c:forEach var="code" items="${st.invalidAirports}">
Invalid Airport Code - <el:cmd url="airport" linkID="${code}" op="edit" target="_new" className="bld">${code}</el:cmd><br />
</c:forEach></td>
</tr>
</c:if>
</c:forEach>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:cmdbutton url="schedimport" label="IMPORT RAW FLIGHT SCHEDULE" />&nbsp;<el:cmdbutton url="schedfilter" label="FILTER FLIGHT SCHEDULE" /></td>
</tr>
</el:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
