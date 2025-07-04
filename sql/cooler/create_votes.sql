CREATE TABLE COOLER_VOTES (
 ID						INTEGER UNSIGNED NOT NULL,
 PILOT_ID				INTEGER UNSIGNED NOT NULL,
 OPT_ID					SMALLINT UNSIGNED NOT NULL,
 PRIMARY KEY (ID, PILOT_ID, OPT_ID),
 FOREIGN KEY (OPT_ID, ID) REFERENCES COOLER_POLLS(OPT_ID,ID) ON DELETE CASCADE,
 FOREIGN KEY (PILOT_ID) REFERENCES USERDATA(ID) ON DELETE CASCADE,
);