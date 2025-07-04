CREATE TABLE NOTAM_IMGS (
 ID                 INTEGER UNSIGNED NOT NULL,
 IMG                MEDIUMBLOB NOT NULL,
 X                  SMALLINT UNSIGNED NOT NULL,
 Y                  SMALLINT UNSIGNED NOT NULL,
 WIDTH              TINYINT UNSIGNED NOT NULL DEFAULT 100,
 TYPE               TINYINT UNSIGNED NOT NULL,
 PRIMARY KEY (ID),
 FOREIGN KEY (ID) REFERENCES NOTAMS(ID) ON DELETE CASCADE
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC;