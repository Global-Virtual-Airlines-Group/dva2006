<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<content:sysdata var="infoEmail" name="airline.mail.info" />
<content:sysdata var="showIssue" name="issue_track.show" />
<content:sysdata var="acarsEnabled" name="acars.enabled" />
<!-- Sidebar Navigation Frame -->
<div id="sidebar">
<el:table ID="sidenav" pad="default" space="default">
<tr class="MenuHeader"><td>ONLINE EVENTS</td></tr>
<tr class="MenuItem">
<c:choose>
<c:when test="${empty pageContext.request.userPrincipal}">
 <td><el:cmd className="bld" url="login">LOG IN</el:cmd></td>
</c:when>
<c:otherwise>
 <td class="MenuItem sec caps">WELCOME, ${pageContext.request.remoteUser}</td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="logout">LOG OUT</el:cmd></td>
</c:otherwise>
</c:choose>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="event">NEXT EVENT</el:cmd></td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="events">ALL EVENTS</el:cmd></td>
</tr>
<c:if test="${access.canCreate}">
<tr class="MenuItem">
 <td><el:cmd url="eventedit">NEW ONLINE EVENT</el:cmd></td>
</tr>
</c:if>
<c:forEach var="nextEvent" items="${futureEvents}">
<tr class="MenuItem">
 <td class="caps"><el:cmd url="event" linkID="0x${nextEvent.ID}">${nextEvent.name}</el:cmd></td>
</tr>
</c:forEach>
<tr class="MenuHeader"><td>ONLINE PILOTS</td></tr>
<tr class="MenuItem">
 <td><el:cmd url="flightboard">WHO IS ONLINE</el:cmd></td>
</tr>
<c:if test="${acarsEnabled}">
<tr class="MenuItem">
 <td><el:cmd url="acarsmap">LIVE ACARS MAP</el:cmd></td>
</tr>
</c:if>
<tr class="MenuHeader"><td>OUR AIRLINE</td></tr>
<tr class="MenuItem">
 <td><el:link url="/">HOME</el:link></td>
</tr>
<content:filter roles="Pilot">
<tr class="MenuItem bld pri">
 <td><el:cmd url="pilotcenter">PILOT CENTER</el:cmd></td>
</tr>
</content:filter>
<tr class="MenuItem">
 <td><el:cmd url="channels">WATER COOLER</el:cmd></td>
</tr>
<tr class="MenuHeader"><td>CONTACT US</td></tr>
<tr class="MenuItem">
 <td><el:link url="mailto:${infoEmail}">CORPORATE OFFICES</el:link></td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="staff">OUR STAFF</el:cmd></td>
</tr>
<c:if test="${showIssue}">
<tr class="MenuHeader"><td>DVA 2006</td></tr>
<tr class="MenuItem">
 <td><el:cmd url="issues" op="Open">ISSUE TRACKER</el:cmd></td>
</tr>
<tr class="MenuItem">
 <td><el:link url="/javadoc/index.html">JAVA API DOCS</el:link></td>
</tr>
<tr class="MenuItem">
 <td><el:link url="/junit/index.html">JUNIT RESULTS</el:link></td>
</tr>
</c:if>
</el:table>
</div>
