CREATE TABLE FLIGHTSTATS_ROUTES (
 PILOT_ID               INTEGER UNSIGNED NOT NULL,
 AIRPORT_D              CHAR(3) NOT NULL,
 AIRPORT_A              CHAR(3) NOT NULL,
 CNT                    INTEGER UNSIGNED NOT NULL,
 LASTFLIGHT             DATE NOT NULL,
 PRIMARY KEY (PILOT_ID, AIRPORT_D, AIRPORT_A),
 FOREIGN KEY (PILOT_ID) REFERENCES PILOTS(ID) ON UPDATE CASCADE ON DELETE CASCADE,
 FOREIGN KEY (AIRPORT_D) REFERENCES common.AIRPORTS(IATA) ON UPDATE CASCADE,
 FOREIGN KEY (AIRPORT_A) REFERENCES common.AIRPORTS(IATA) ON UPDATE CASCADE
);
