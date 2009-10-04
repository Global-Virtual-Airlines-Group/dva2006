#!/bin/bash
# VATSIM Usage tracker script

JAVA_HOME=/usr/local/java
CPATH=log4j-1.2.15.jar:jdom-1.1.1.jar:mysql-connector-java-5.1.10-bin.jar
INFO=/var/cache/servinfo/vatsim.info

# Start the JVM
cd /usr/local/usageTrack
$JAVA_HOME/bin/java -server -Xmx40m -Xbootclasspath/a:$CPATH -jar usageTracker.jar $INFO >>/var/log/usageTrack.log 2>&1 &
echo $! > /var/run/usageTrack.pid
exit 0
