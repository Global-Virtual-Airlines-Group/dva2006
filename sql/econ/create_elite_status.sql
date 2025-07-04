CREATE TABLE ELITE_STATUS (
 PILOT_ID               INTEGER UNSIGNED NOT NULL,
 NAME                   VARCHAR(32) NOT NULL,
 YR                     SMALLINT UNSIGNED NOT NULL,
 CREATED                DATETIME NOT NULL,
 UPD_REASON             TINYINT UNSIGNED NOT NULL DEFAULT 0,
 PRIMARY KEY (PILOT_ID, NAME, YR),
 FOREIGN KEY (PILOT_ID) REFERENCES PILOTS(ID) ON DELETE CASCADE ON UPDATE CASCADE,
 FOREIGN KEY (NAME,YR) REFERENCES ELITE_LEVELS(NAME,YR) ON DELETE CASCADE ON UPDATE CASCADE
) CHARACTER SET latin1;

CREATE INDEX ELITE_STATUS_YR_IDX ON ELITE_STATUS(YR);