#!/bin/sh
# Delta Virtual Airlines backup script
# (c) 2005 Luke J. Kolin. All Rights Reserved.

# Create backup directory
cd /tmp
mkdir backup
cd backup

# Dump Common data/Water Cooler/Events
mysqldump -C -n -t -K -h mysql.deltava.org common AIRLINEINFO TZ AIRLINES AIRPORTS AIRPORT_AIRLINE OCEANIC ISSUES ISSUE_COMMENTS SYSINFODATA | gzip -9 > common.sql.gz
mysqldump -C -n -t -K -h mysql.deltava.org common COOLER_CHANNELS COOLER_CHANNELINFO COOLER_THREADS COOLER_POSTS COOLER_NOTIFY | gzip -9 > cooler.sql.gz
mysqldump -C -n -t -K -h mysql.deltava.org common EVENTS EVENT_AIRPORTS EVENT_CHARTS EVENT_PLANS EVENT_EQTYPES EVENT_SIGNUPS | gzip -9 > events.txt

# Dump Navigation Data if we're doing a full backup
if ["$1" == "full"] ;then
  mysqldump -C -n -t -K -h mysql.deltava.org common NAVDATA CHARTS | gzip -9 > navdata.sql.gz
fi

# Dump ACARS data
mysqldump -C -n -t -K -h mysql.deltava.org acars CONS FLIGHTS POSITIONS MESSAGES | gzip -9 > acars.sql.gz

# Dump DVA database
mysqldump -C -n -t -K -h mysql.deltava.org dva PILOTS RATINGS ROLES PIREPS ACARS_PIREPS PROMO_EQ ASSIGNMENTS ASSIGNLEGS DOCS FLEET EQTYPES EQRATINGS NEWS NOTAMS PILOT_MAP SIGNATURES STAFF STATUS_UPDATES | gzip -9 > dva.sql.gz
mysqldump -C -n -t -K -h mysql.deltava.org dva APPLICANTS APPEXAMS APPQUESTIONS CHECKRIDES EXAMINFO QUESTIONINFO QE_INFO EXAMS EXAMQUESTIONS TXREQUESTS | gzip -9 > dva_test.sql.gz
mysqldump -C -n -t -K -h mysql.deltava.org dva DOWNLOADS INACTIVITY HELP LOG_APP LOG_TASK MSG_TEMPLATES EMAIL_VALIDATION SYS_COMMANDS SYS_HTTPLOG SYS_SESSIONS SYS_TASKS | gzip -9 > dva_sys.sql.gz
mysqldump -C -n -t -K -h mysql.deltava.org dva SCHEDULE ROUTES | gzip -9 > dva_sched.sql.gz

# Dump AFV database
mysqldump -C -n -t -K -h mysql.deltava.org afv PILOTS RATINGS ROLES PIREPS ACARS_PIREPS PROMO_EQ ASSIGNMENTS ASSIGNLEGS DOCS FLEET EQTYPES EQRATINGS NEWS NOTAMS PILOT_MAP SIGNATURES STAFF STATUS_UPDATES | gzip -9 > afv.sql.gz
mysqldump -C -n -t -K -h mysql.deltava.org afv APPLICANTS APPEXAMS APPQUESTIONS CHECKRIDES EXAMINFO QUESTIONINFO QE_INFO EXAMS EXAMQUESTIONS TXREQUESTS | gzip -9 > afv_test.sql.gz
mysqldump -C -n -t -K -h mysql.deltava.org afv DOWNLOADS INACTIVITY HELP LOG_APP LOG_TASK MSG_TEMPLATES EMAIL_VALIDATION SYS_COMMANDS SYS_HTTPLOG SYS_SESSIONS SYS_TASKS | gzip -9 > afv_sys.sql.gz
mysqldump -C -n -t -K -h mysql.deltava.org afv SCHEDULE ROUTES | gzip -9 > afv_sched.sql.gz

# Dump Galleries if we're doing a full backup
if ["$1" == "full"] ;then
  mysqldump -C -n -t -K -h mysql.deltava.org dva GALLERY GALLERYSCORE | gzip -9 > dva_gallery.sql.gz
  mysqldump -C -n -t -K -h mysql.deltava.org afv GALLERY GALLERYSCORE | gzip -9 > afv_gallery.sql.gz
fi

# If we're doing a full backup, shift the parameters so we can read $2
if ["$1" == "full"] ;then
  shift
fi

# Check if we need to ftp the files somewhere
if ["$1" == "ftp"] ;then
  if [-z "$2"] ;then
    echo "No FTP host specified"
    exit 1
  fi

# FTP the file away
  ftp $2 < /usr/local/etc/ftp_backup.conf
fi
