<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Flight Schedule Filter</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<script async>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.effDate,l:10,t:'Effective Date'});
	golgotha.form.validate({f:f.src,min:1,t:'Schedule Source'});
	golgotha.form.submit(f);
	return true;
};

golgotha.local.updateSource = function(cb) {
	golgotha.util.display(document.getElementById('src-' + cb.value), cb.checked);
	return true;
};

golgotha.onDOMReady(function() {
	const f = document.forms[0];
	f.src.forEach(function(cb) { golgotha.local.updateSource(cb); });
});
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="schedfilter.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<c:if test="${!empty status}">
<tr class="title caps">
 <td colspan="2"><span class="nophone"><content:airline />&nbsp;</span>FLIGHT SCHEDULE IMPORT STATUS</td>
</tr>
<tr>
 <td class="label top">Import Results</td>
 <td class="data"><fmt:int value="${importCount}" /> Raw Schedule Entries loaded (<fmt:int value="${purgeCount}" /> purged) from <span class="sec bld">${status.source.description}</span></td>
</tr>
<c:if test="${!empty status.errorMessages}">
<tr>
 <td class="label top">Import Messages</td>
 <td class="data small"><c:forEach var="msg" items="${status.errorMessages}">${msg}<br /></c:forEach></td>
</tr>
</c:if>
</c:if>
<tr class="title caps">
 <td colspan="2"><span class="nophone"><content:airline />&nbsp;</span>FLIGHT SCHEDULE FILTER</td>
</tr>
<tr>
 <td class="label">Sources</td>
 <td class="data"><el:check name="src" width="210" options="${sources}" value="${sources}" onChange="void golgotha.local.updateSource(this)" /></td>
</tr>
<c:forEach var="src" items="${sources}">
<c:set var="selectedAirlines" value="${srcAirlines[src.source]}" scope="page" />
<tr id="src-${src.source}" style="display:none;">
 <td class="label top">${src.source.description}</td>
 <td class="data"><el:check name="airline-${src.source}"  width="210" cols="5" newLine="true" options="${src.options}" value="${selectedAirlines}" /></td>
</tr>
</c:forEach>
<tr>
 <td class="label">Effective Date</td>
 <td class="data"><el:text name="effDate" size="9" max="10" required="true" value="${fn:dateFmt(today, 'MM/dd/yyyy')}" /></td>
</tr>
</el:table>

<!-- Button bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="FILTER FLIGHT SCHEDULE" />&nbsp;<el:cmdbutton url="schedimport" label="IMPORT RAW SCHEDULE ENTRIES" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>
