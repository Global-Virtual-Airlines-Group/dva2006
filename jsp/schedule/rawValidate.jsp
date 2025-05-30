<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Raw Schedule Validation</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:favicon />
<content:js name="common" />
<content:googleAnalytics />
<fmt:aptype var="useICAO" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script async>
golgotha.local.validate = function(f) {
	if ((!golgotha.form.check()) || (!f.comments)) return false;
	golgotha.form.validate({f:f.src, t:'Raw Schedule Source'});
	golgotha.form.submit(f);
	return true;
};

golgotha.local.setSrc = function(cb) {
	self.location = '/rawvalidate.do?id=' + encodeURI(golgotha.form.getCombo(cb));
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="rawvalidate.do" method="post" validate="return false">
<view:table cmd="rawvalidate">

<!-- Table Header Bars -->
<tr class="title caps">
 <td class="left" colspan="5"><span class="nophone"><content:airline />&nbsp;</span>RAW FLIGHT SCHEDULE VALIDATION</td>
 <td class="right" colspan="6"><span class="nophone">SCHEDULE SOURCE </span><el:combo name="src" idx="*" size="1" required="true" firstEntry="[ SCHEDULE SOURCE ]" value="${srcInfo.source}" options="${srcs}" onChange="void golgotha.local.setSrc(this)" /></td>
</tr>

<tr class="title caps">
 <td class="nophone">LINE</td>
 <td style="width:10%">FLIGHT NUMBER</td>
 <td style="width:9%">EFFECTIVE</td>
 <td style="width:5%">DAYS</td>
 <td>EQUIPMENT</td>
 <td>AIRPORTS</td>
 <td class="nophone" style="width:6%">DEPARTS</td>
 <td class="nophone" style="width:6%">ARRIVES</td>
 <td class="nophone" style="width:7%">DISTANCE</td>
 <td style="width:6%">DURATION</td>
 <td class="nophone">ISSUE</td>
</tr>

<!-- Table Data Section -->
<c:forEach var="entry" items="${viewContext.results}">
<c:set var="vr" value="${results[entry]}" scope="page" />
<view:row entry="${entry}">
 <td class="small nophone">${entry.lineNumber}</td>
 <td class="pri bld"><el:cmd url="sched" linkID="${entry.source}-${entry.lineNumber}">${entry.flightCode}</el:cmd></td>
 <td class="small"><fmt:date fmt="d" d="MM/dd/yy" date="${entry.startDate}" tzName="UTC" /> - <fmt:date fmt="d" d="MM/dd/yy" date="${entry.endDate}" tzName="UTC" /></td>
 <td class="small sec">${entry.dayCodes}</td>
 <td class="sec bld">${entry.equipmentType}</td>
 <td class="small">${entry.airportD.name} (<el:cmd url="airportinfo" linkID="${entry.airportD.IATA}" className="plain"><fmt:airport airport="${entry.airportD}" /></el:cmd>) to ${entry.airportA.name} (<el:cmd url="airportinfo" linkID="${entry.airportA.IATA}" className="plain"><fmt:airport airport="${entry.airportA}" /></el:cmd>)</td>
 <td class="nophone"><fmt:date fmt="t" t="HH:mm" tz="${entry.airportD.TZ}" date="${entry.timeD}" /></td>
 <td class="nophone"><fmt:date fmt="t" t="HH:mm" tz="${entry.airportA.TZ}" date="${entry.timeA}" /></td>
 <td class="sec nophone"><fmt:distance value="${entry.distance}" /></td>
 <td><fmt:duration duration="${entry.duration}" t="HH:mm" /></td>
 <td class="warn bld nophone">${vr.message}</td>
</view:row>
</c:forEach>

<!-- Scroll bar -->
<tr class="title">
 <td colspan="11"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>
 <view:legend width="150" labels="Regular Flight,Historic Flight" classes=" ,opt2" /></td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>
