CREATE TABLE ISSUE_AIRLINES (
 ID              INTEGER UNSIGNED NOT NULL,
 AIRLINE         CHAR(3) NOT NULL,
 PRIMARY KEY (ID, AIRLINE),
 FOREIGN KEY (ID) REFERENCES ISSUES(ID) ON UPDATE CASCADE ON DELETE CASCADE,
 FOREIGN KEY (AIRLINE) REFERENCES AIRLINEINFO(CODE) ON DELETE CASCADE ON UPDATE CASCADE
) CHARACTER SET latin1;