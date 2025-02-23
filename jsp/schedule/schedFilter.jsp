<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Flight Schedule Filter</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:js name="datePicker" />
<script>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.src,min:1,t:'Schedule Source'});
	golgotha.form.validate({f:f.doPurge, t:'Schedule Purge Options'});
	f.src.forEach(function(cb) { if (cb.checked) golgotha.form.validate({f:f[cb.value + '-effDate'], l:10,t:'Effective Date'}); });
	golgotha.form.submit(f);
	return true;
};

golgotha.local.updateSource = function(cb) {
	const rows = golgotha.util.getElementsByClass('src-' + cb.value, 'tr');
	rows.forEach(function(r) { golgotha.util.display(r, cb.checked); });
	return true;
};

golgotha.local.toggleAll = function(src) {
	const f = document.forms[0];
	const boxes = f['airline-' + src];
	boxes.forEach(function(cb) { cb.checked = !cb.checked; });
	return true;
};

golgotha.onDOMReady(function() {
	document.forms[0].src.forEach(function(cb) { golgotha.local.updateSource(cb); });
	return true;
});
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>
<content:sysdata var="dateFmt" name="time.date_format" />
<content:enum var="purgeOpts" className="org.deltava.beans.schedule.PurgeOptions" />

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
 <td class="data"><fmt:int value="${importCount}" /> Raw Schedule Entries loaded (<fmt:int value="${purgeCount}" /> purged) from <span class="sec bld">${status.source.description}</span>.<c:if test="${dupeCount > 0}"><fmt:int value="${dupeCount}" /> duplicate entries removed.</c:if></td>
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
 <td class="data"><el:check name="src" width="240" options="${sources}" value="${loadedSources}" onChange="void golgotha.local.updateSource(this)" /></td>
</tr>
<tr>
 <td class="label">Purge Options</td>
 <td class="data"><el:combo name="doPurge" idx="*" required="true" size="1" firstEntry="[ PURGE OPTIONS ]" options="${purgeOpts}" /></td>
</tr>
<c:forEach var="src" items="${sources}">
<c:set var="srcEffName" value="eff${src.source}"  scope="page" />
<tr class="src-${src.source} title caps" style="display:none;">
 <td colspan="2">${src.source.description}<c:if test="${!empty src.effectiveDate}"> - <c:if test="${src.active}">(ACTIVE) </c:if> EFFECTIVE <fmt:date fmt="d" date="${src.effectiveDate}" tzName="UTC" /></c:if>
<c:if test="${!empty src.date}"> (${src.autoImport ? 'AUTO' : 'MANUAL'} IMPORT ON <fmt:date t="HH:mm" date="${src.date}" />)</c:if></td>
</tr>
<tr class="src-${src.source}" style="display:none;">
 <td class="label">Effective Date</td>
 <td class="data"><el:text name="${srcEffName}" size="9" max="10" idx="*" required="true" value="${fn:dateFmt(src.nextImportDate, dateFmt)}" />&nbsp;<el:button label="CALENDAR" onClick="void show_calendar('forms[0].${srcEffName}')" />
<c:if test="${!src.isCurrent && (!empty src.baseDate)}"><span class="nophone" > (Base Date: <fmt:date fmt="d" date="${src.baseDate}" tzName="UTC" />)</span></c:if></td>
</tr>
<tr class="src-${src.source}" style="display:none;">
 <td class="label top">Airlines</td>
 <td class="data"><el:check name="airline-${src.source}"  width="240" cols="5" newLine="true" options="${src.options}" value="${srcAirlines[src.source]}" />
<c:if test="${src.options.size() > 6}"><a href="javascript:golgotha.local.toggleAll('${src.source}')">TOGGLE ALL</a></c:if></td>
</tr>
</c:forEach>
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
