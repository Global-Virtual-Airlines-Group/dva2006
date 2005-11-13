<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<content:sysdata var="infoEmail" name="airline.mail.info" />
<!-- Sidebar Navigation Frame -->
<content:region id="sidebar">
<el:table ID="sidenav" pad="default" space="default">
<tr class="MenuHeader"><td>WATER COOLER</td></tr>
<tr class="MenuItem">
<c:choose>
<c:when test="${empty pageContext.request.userPrincipal}">
 <td><el:cmd className="bld" url="login">LOG IN</el:cmd></td>
</c:when>
<c:otherwise>
 <td class="sec caps">WELCOME, ${pageContext.request.remoteUser}</td>
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
 <td><el:cmd url="channel" linkID="ALL">ALL POSTS</el:cmd></td>
</tr>
<content:filter roles="Pilot">
<tr class="MenuItem">
 <td><el:cmd url="newthreads">NEW THREADS</el:cmd></td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="mythreads">MY THREADS</el:cmd></td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="notifythreads">MY WATCHED THREADS</el:cmd></td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="threadpost">START NEW THREAD</el:cmd></td>
</tr>
</content:filter>
<tr class="MenuItem">
 <td><el:cmd url="channels">CHANNELS</el:cmd></td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="coolersearch">SEARCH</el:cmd></td>
</tr>
<content:filter roles="Admin">
<tr class="MenuItem">
 <td><el:cmd url="channeladmin">CHANNEL ADMIN</el:cmd></td>
</tr>
</content:filter>
<tr class="MenuHeader"><td>OUR AIRLINE</td></tr>
<tr class="MenuItem">
 <td><el:link url="/">HOME</el:link></td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="notams">NOTAMs</el:cmd></td>
</tr>
<content:filter roles="Pilot">
<tr class="MenuItem bld pri">
 <td><el:cmd url="pilotcenter">PILOT CENTER</el:cmd></td>
</tr>
</content:filter>
<tr class="MenuItem">
 <td><el:cmd url="event">ONLINE EVENTS</el:cmd></td>
</tr>
<tr class="MenuHeader"><td>STATISTICS</td></tr>
<tr class="MenuItem">
 <td><el:cmd url="airlinestats">AIRLINE TOTALS</el:cmd></td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="httpstats">SERVER STATISTICS</el:cmd></td>
</tr>
<content:filter roles="Moderator">
<tr class="MenuItem">
 <td><el:cmd url="coolerstats">COOLER STATISTICS</el:cmd></td>
</tr>
</content:filter>
<tr class="MenuHeader"><td>CONTACT US</td></tr>
<tr class="MenuItem">
 <td><el:link url="mailto:${infoEmail}">CORPORATE OFFICES</el:link></td>
</tr>
<tr class="MenuItem">
 <td><el:cmd url="staff">OUR STAFF</el:cmd></td>
</tr>
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
