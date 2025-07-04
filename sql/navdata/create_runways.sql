CREATE TABLE RUNWAYS (
 ICAO				CHAR(4) NOT NULL,
 NAME				VARCHAR(4) NOT NULL,
 SIMVERSION			SMALLINT UNSIGNED NOT NULL,
 LATITUDE			DECIMAL (10,6) NOT NULL,
 LONGITUDE			DECIMAL (10,6) NOT NULL,
 HDG				SMALLINT UNSIGNED NOT NULL,
 LENGTH				INTEGER UNSIGNED NOT NULL,
 WIDTH				SMALLINT UNSIGNED NOT NULL,
 MAGVAR				DECIMAL (3,1) NOT NULL,
 SURFACE            TINYINT UNSIGNED NOT NULL DEFAULT 0,
 THRESHOLD          SMALLINT UNSIGNED NOT NULL DEFAULT 0,
 LL                 POINT NOT NULL SRID 4326,
 PRIMARY KEY (ICAO, NAME, SIMVERSION)
);

CREATE SPATIAL INDEX RUNWAY_GEO_IDX ON RUNWAYS(LL);