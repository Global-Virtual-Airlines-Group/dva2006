CREATE TABLE GATE_AIRLINES (
 ICAO               CHAR(4) NOT NULL,
 NAME               VARCHAR(16) NOT NULL,
 AIRLINE            CHAR(3) NOT NULL,
 ZONE               TINYINT UNSIGNED NOT NULL DEFAULT 0,
 PRIMARY KEY (ICAO, NAME, AIRLINE),
 FOREIGN KEY (ICAO, NAME) REFERENCES GATES(ICAO, NAME) ON UPDATE CASCADE ON DELETE CASCADE,
 FOREIGN KEY (AIRLINE) REFERENCES AIRLINES(CODE) ON DELETE CASCADE ON UPDATE CASCADE
);