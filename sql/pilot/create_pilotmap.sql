CREATE TABLE PILOT_MAP (
 ID					INTEGER UNSIGNED NOT NULL,
 LAT					FLOAT(10) NOT NULL,
 LNG					FLOAT(10) NOT NULL,
 H						FLOAT(10) NOT NULL,
 PRIMARY KEY (ID),
 FOREIGN KEY (ID) REFERENCES common.USERDATA(ID)
);
