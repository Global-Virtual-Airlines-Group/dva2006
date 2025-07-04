CREATE TABLE IF NOT EXISTS EVENT_SIGNUPS (
 ID				INTEGER UNSIGNED NOT NULL,
 ROUTE_ID		INTEGER UNSIGNED NOT NULL,
 PILOT_ID		INTEGER UNSIGNED NOT NULL,
 EQTYPE			VARCHAR(15) NOT NULL,
 REMARKS		VARCHAR(224) CHARACTER SET utf8mb4,
 PRIMARY KEY (ID, PILOT_ID),
 FOREIGN KEY (ID, ROUTE_ID) REFERENCES EVENT_AIRPORTS(ID, ROUTE_ID) ON DELETE CASCADE,
 FOREIGN KEY (PILOT_ID) REFERENCES common.USERDATA(ID) ON DELETE CASCADE,
 FOREIGN KEY (EQTYPE) REFERENCES common.AIRCRAFT(NAME) ON UPDATE CASCADE
);

CREATE INDEX EV_SRT_IDX ON EVENT_SIGNUPS(ID, ROUTE_ID);