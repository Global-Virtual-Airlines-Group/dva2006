CREATE TABLE JOBCOMMENTS (
 ID					INTEGER UNSIGNED NOT NULL,
 AUTHOR_ID			INTEGER UNSIGNED NOT NULL,
 CREATED			DATETIME NOT NULL,
 BODY				TEXT CHARACTER SET utf8mb4 NOT NULL,
 PRIMARY KEY (ID, CREATED),
 FOREIGN KEY (ID) REFERENCES JOBPOSTINGS(ID) ON UPDATE CASCADE ON DELETE CASCADE,
 FOREIGN KEY (AUTHOR_ID) REFERENCES PILOTS(ID) ON UPDATE CASCADE
);