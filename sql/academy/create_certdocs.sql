CREATE TABLE CERTDOCS (
 FILENAME			VARCHAR(64) NOT NULL,
 CERT				VARCHAR(8) NOT NULL,
 PRIMARY KEY (FILENAME, CERT),
 FOREIGN KEY (CERT) REFERENCES CERTS(ABBR) ON DELETE CASCADE ON UPDATE CASCADE
);