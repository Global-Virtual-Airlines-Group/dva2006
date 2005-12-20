<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<content:sysdata var="infoEmail" name="airline.mail.info" />
<content:sysdata var="acarsEnabled" name="acars.enabled" />
<content:sysdata var="newsletterEnabled" name="airline.newsletters.enabled" />
<content:sysdata var="newsletter" name="airline.newsletters.name" />
<!-- Sidebar Navigation Frame -->
<content:region id="sidebar">
<el:table ID="sidenav" pad="default" space="default">
<tr class="MenuHeader"><td>OUR AIRLINE</td></tr>
<tr class="MenuItem">
 <td><el:link url="/">HOME</el:link></td>
</tr>
<tr class="MenuItem">
<c:choose>
<c:when test="${empty pageContext.request.userPrincipal}">
 <td><el:cmd className="bld" url="login">LOG IN</el:cmd></td>
</c:when>
<c:otherwise>
 <td class="MenuItem sec caps">WELCOME, ${pageContext.request.remoteUser}</td>
</tr>
<c:if test="${!empty superUser}">
<tr class="MenuItem">
 <td class="ter bld">${superUser.name}</td>
</tr>
</c:if>
<tr class="MenuItem">
 <td><el:cmd url="logout">LOG OUT</el:cmd></td>
</c:otherwise>
</c:choose>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="notams">NOTAMs</el:cmd></td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="news">AIRLINE NEWS</el:cmd></td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="fleetgallery">FLEET GALLERY</el:cmd></td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="imagegallery">IMAGE GALLERY</el:cmd></td>
</tr>
<c:if test="${newsletterEnabled}">
<tr class="MenuItem caps">
 <td><el:cmd url="newsletters">${newsletter}</el:cmd></td>
</tr>
</c:if>
<tr class="MenuItem">
 <td><el:cmd url="flightboard">WHO IS ONLINE</el:cmd></td>
</tr>
<c:if test="${acarsEnabled}">
<tr class="MenuItem">
 <td><el:cmd url="acarsmap">LIVE ACARS MAP</el:cmd></td>
</tr>
</c:if>
<tr class="MenuItem">
 <td><el:cmd url="users">LOGGED IN USERS</el:cmd></td>
</tr>
<tr class="MenuHeader"><td>PILOTS' LOUNGE</td></tr>
<content:filter roles="Pilot">
<tr class="MenuItem bld pri">
 <td><el:cmd url="pilotcenter">PILOT CENTER</el:cmd></td>
</tr>
</content:filter>
<tr class="MenuItem">
 <td><el:cmd url="event">ONLINE EVENTS</el:cmd></td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="channels">WATER COOLER</el:cmd></td>
</tr>
<tr class="MenuHeader"><td>OUR PEOPLE</td></tr>
<c:if test="${empty pageContext.request.userPrincipal}">
<tr class="MenuItem bld pri">
 <td><el:cmd url="register">JOIN US</el:cmd></td>
</tr>
</c:if>
<tr class="MenuItem">
 <td><el:cmd url="roster">PILOT ROSTER</el:cmd></td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="pilotboard">PILOT LOCATIONS</el:cmd></td>
</tr>
<content:filter roles="HR,PIREP,Examination">
<tr class="MenuItem">
 <td><el:cmd url="pilotsearch">PILOT SEARCH</el:cmd></td>
</tr>
</content:filter>
<tr class="MenuItem">
 <td><el:cmd url="recognition">PILOT ACCOMPLISHMENTS</el:cmd></td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="centuryclub">CENTURY CLUB</el:cmd></td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="landings">GREASED LANDINGS</el:cmd></td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="lroster">VIEW LOGBOOKS</el:cmd></td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="acarsprsearch">ACARS FLIGHTS</el:cmd></td>
</tr>
<tr class="MenuHeader"><td>STATISTICS</td></tr>
<tr class="MenuItem">
 <td><el:cmd url="airlinestats">AIRLINE TOTALS</el:cmd></td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="flightstats">FLIGHT TOTALS</el:cmd></td>
</tr>
<content:filter roles="Fleet,HR">
<tr class="MenuItem">
 <td><el:cmd url="fleetstats">FLEET TOTALS</el:cmd></td>
</tr>
</content:filter>
<content:filter roles="Moderator,HR">
<tr class="MenuItem">
 <td><el:cmd url="coolerstats">COOLER STATISTICS</el:cmd></td>
</tr>
</content:filter>
<tr class="MenuItem">
 <td><el:cmd url="httpstats">SERVER STATISTICS</el:cmd></td>
</tr>
<tr class="MenuHeader"><td>CONTACT US</td></tr>
<tr class="MenuItem">
 <td><el:link url="mailto:${infoEmail}">CORPORATE OFFICES</el:link></td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="staff">OUR STAFF</el:cmd></td>
</tr>
<content:filter roles="Anonymous,HR,Admin">
<tr class="MenuItem">
 <td><el:cmd url="pwdreset">PASSWORD RESET</el:cmd></td>
</tr>
</content:filter>
<tr class="MenuHeader"><td>GOLGOTHA</td></tr>
<tr class="MenuItem">
 <td><el:cmd url="issues" op="Open">ISSUE TRACKER</el:cmd></td>
</tr>
<tr class="MenuItem">
 <td><el:link url="http://www.gvagroup.org/javadoc/index.html">JAVA API DOCS</el:link></td>
</tr>
<tr class="MenuItem">
 <td><el:link url="http://www.gvagroup.org/junit/index.html">JUNIT RESULTS</el:link></td>
</tr>
</el:table>
</content:region>
