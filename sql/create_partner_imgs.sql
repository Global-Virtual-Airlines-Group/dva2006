CREATE TABLE PARTNER_IMGS (
 ID                 INTEGER UNSIGNED NOT NULL,
 IMG                MEDIUMBLOB NOT NULL,
 X                  SMALLINT UNSIGNED NOT NULL,
 Y                  SMALLINT UNSIGNED NOT NULL,
 EXT                CHAR(3) NOT NULL,
 PRIMARY KEY (ID),
 FOREIGN KEY (ID) REFERENCES PARTNERS(ID) ON DELETE CASCADE
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC CHARACTER SET latin1;