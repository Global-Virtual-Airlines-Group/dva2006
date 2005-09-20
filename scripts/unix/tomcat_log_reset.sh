#!/bin/sh
# Delta Virtual Airlines Tomcat log reset script
# (c) 2005 Luke J. Kolin. All Rights Reserved.

rm /usr/local/jakarta-tomcat5.0/logs/catalina.out
touch /usr/local/jakarta-tomcat5.0/logs/catalina.out
chown 80 /usr/local/jakarta-tomcat5.0/logs/catalina.out
