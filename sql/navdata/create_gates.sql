CREATE TABLE GATES (
 ICAO				CHAR(4) NOT NULL,
 NAME				VARCHAR(16) NOT NULL,
 LATITUDE			DECIMAL (10,6) NOT NULL,
 LONGITUDE			DECIMAL (10,6) NOT NULL,
 HDG				INTEGER UNSIGNED NOT NULL,
 PRIMARY KEY (ICAO, NAME)
) CHARACTER SET latin1;