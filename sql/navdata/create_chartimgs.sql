CREATE TABLE CHARTIMGS (
 ID					INTEGER UNSIGNED NOT NULL,
 IMG				MEDIUMBLOB NOT NULL,
 PRIMARY KEY (ID),
 FOREIGN KEY (ID) REFERENCES CHARTS(ID) ON UPDATE CASCADE ON DELETE CASCADE
);
