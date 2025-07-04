CREATE TABLE PIREP_STATUS_HISTORY (
 ID                  INTEGER UNSIGNED NOT NULL,
 AUTHOR_ID           INTEGER UNSIGNED NOT NULL DEFAULT 0,
 UPDATE_TYPE         SMALLINT UNSIGNED NOT NULL DEFAULT 0,
 CREATEDON           DATETIME(3) NOT NULL,
 DESCRIPTION         TEXT CHARACTER SET utf8mb4 NOT NULL,
 PRIMARY KEY (ID, CREATEDON),
 FOREIGN KEY (ID) REFERENCES PIREPS(ID) ON UPDATE CASCADE ON DELETE CASCADE
) CHARACTER SET latin1;