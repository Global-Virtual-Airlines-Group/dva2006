CREATE TABLE PILOT_IMADDR (
 ID					INTEGER UNSIGNED NOT NULL,
 TYPE				VARCHAR(16) NOT NULL,
 ADDR				VARCHAR(252) NOT NULL,
 PRIMARY KEY (ID, TYPE),
 FOREIGN KEY (ID) REFERENCES PILOTS(ID) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX PILOT_IMADDR_IDX ON PILOT_IMADDR(ADDR);
