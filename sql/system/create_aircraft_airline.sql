CREATE TABLE AIRCRAFT_AIRLINE (
 NAME					VARCHAR(15) NOT NULL,
 AIRLINE				CHAR(3) NOT NULL,
 ACRANGE				SMALLINT UNSIGNED NOT NULL,
 ETOPS					BOOLEAN NOT NULL DEFAULT FALSE,
 SEATS					SMALLINT UNSIGNED NOT NULL DEFAULT 0,
 TO_RWLENGTH			SMALLINT UNSIGNED NOT NULL DEFAULT 0,
 LN_RWLENGTH			SMALLINT UNSIGNED NOT NULL DEFAULT 0,
 SOFT_RWY               BOOLEAN NOT NULL DEFAULT FALSE,
 PRIMARY KEY (NAME, AIRLINE),
 FOREIGN KEY (NAME) REFERENCES AIRCRAFT(NAME) ON DELETE CASCADE ON UPDATE CASCADE,
 FOREIGN KEY (AIRLINE) REFERENCES AIRLINEINFO(CODE) ON DELETE CASCADE ON UPDATE CASCADE
);